package com.santander.msclaimsservices.services;

import com.santander.msclaimsservices.clients.PolicyServicesClients;
import com.santander.msclaimsservices.clients.ProtocolSequencialClients;
import com.santander.msclaimsservices.clients.UserServicesClients;
import com.santander.msclaimsservices.constants.ClaimConstants;
import com.santander.msclaimsservices.dto.ClaimRequestStatusDto;
import com.santander.msclaimsservices.dto.ClaimResponseDto;
import com.santander.msclaimsservices.dto.PolicyResponseDto;
import com.santander.msclaimsservices.exception.BadRequestException;
import com.santander.msclaimsservices.exception.BusinessException;
import com.santander.msclaimsservices.exception.NotFoundException;
import com.santander.msclaimsservices.model.Claim;
import com.santander.msclaimsservices.model.ClaimStatus;
import com.santander.msclaimsservices.repository.ClaimRepository;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServicesTest {

    private static final String VALID_CLAIM_REQUEST_JSON = "{"
            + "\"policyNumber\":\"PROTOCOL-001\","
            + "\"description\":\"Furto de veiculo\","
            + "\"eventDate\":\"01/07/2026 10:30\","
            + "\"openingDate\":\"02/07/2026 09:00\","
            + "\"claimedAmount\":1500.00"
            + "}";

    @Mock
    private Validator validator;

    @Mock
    private S3Servicos s3Servicos;

    @Mock
    private MessageSource messageSource;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private AttachmentServices attachmentServices;

    @Mock
    private UserServicesClients userServicesClients;

    @Mock
    private PolicyServicesClients policyServicesClients;

    @Mock
    private ProtocolSequencialClients protocolSequencialClients;

    @InjectMocks
    private ClaimServices claimServices;

    private final Locale locale = new Locale("pt", "BR");

    private PolicyResponseDto policy;
    private Claim savedClaim;

    @BeforeEach
    void setUp() {
        policy = PolicyResponseDto.builder()
                .policyNumber("PROTOCOL-001")
                .build();

        savedClaim = Claim.builder()
                .id(BigInteger.ONE)
                .claimNumber("SIN-001")
                .status(ClaimStatus.OPEN.name())
                .description("Furto de veiculo")
                .claimedAmount(new java.math.BigDecimal("1500.00"))
                .build();
    }

    private void stubValidationPasses() {
        when(validator.validate(any())).thenReturn(Set.of());
    }

    @Nested
    @DisplayName("newClaimDto")
    class NewClaimDto {

        @Test
        @DisplayName("deve criar o sinistro com sucesso quando não há anexos")
        void shouldCreateClaimSuccessfullyWithoutAttachments() {
            stubValidationPasses();
            when(policyServicesClients.findByPolicyNumber("PROTOCOL-001")).thenReturn(policy);
            when(protocolSequencialClients.generateProtocol("SIN")).thenReturn("SIN-001");
            when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

            ClaimResponseDto response = claimServices.newClaimDto(
                    Collections.emptyList(), VALID_CLAIM_REQUEST_JSON, locale);

            assertThat(response).isNotNull();
            assertThat(response.getClaimNumber()).isEqualTo("SIN-001");
            assertThat(response.getStatus()).isEqualTo(ClaimStatus.OPEN.name());

            verify(attachmentServices, never()).newAttachment(any());
            verify(rabbitTemplate, times(1)).convertAndSend(
                    eq(ClaimConstants.RABBIT_QUEUE_CLAIM_EXCHANGE),
                    eq(ClaimConstants.RABBIT_QUEUE_CLAIM_ROUTER),
                    any(ClaimResponseDto.class));
        }

        @Test
        @DisplayName("deve fazer upload e criar anexos quando arquivos são enviados")
        void shouldUploadAndCreateAttachmentsWhenFilesPresent() throws Exception {
            stubValidationPasses();
            when(policyServicesClients.findByPolicyNumber("PROTOCOL-001")).thenReturn(policy);
            when(protocolSequencialClients.generateProtocol("SIN")).thenReturn("SIN-001");
            when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);
            when(s3Servicos.upload(any(MultipartFile.class), eq("SIN-001"))).thenReturn("s3-key-1");
            when(attachmentServices.newAttachment(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "documento.jpg", "image/jpeg", "conteudo".getBytes());

           try (MockedStatic<com.santander.msclaimsservices.util.FileValidator> fileValidatorMock =
                         mockStatic(com.santander.msclaimsservices.util.FileValidator.class)) {

                ClaimResponseDto response = claimServices.newClaimDto(
                        List.of(file), VALID_CLAIM_REQUEST_JSON, locale);

                assertThat(response).isNotNull();
                assertThat(response.getClaimNumber()).isEqualTo("SIN-001");

                fileValidatorMock.verify(() -> com.santander.msclaimsservices.util.FileValidator.validate(file));
            }

            verify(s3Servicos, times(1)).upload(any(MultipartFile.class), eq("SIN-001"));
            verify(attachmentServices, times(1)).newAttachment(anyList());
        }

        @Test
        @DisplayName("deve envolver em BusinessException com rollback quando algo falha no fluxo")
        void shouldWrapFailureInRollbackBusinessException() {
            stubValidationPasses();
            when(policyServicesClients.findByPolicyNumber("PROTOCOL-001"))
                    .thenThrow(new BusinessException("policy indisponivel"));
            when(messageSource.getMessage(eq(ClaimConstants.POLICY_ERROR_TO_FIND), any(), eq(locale)))
                    .thenReturn("Erro ao consultar apolice");
            when(messageSource.getMessage(eq(ClaimConstants.CLAIM_ERROR_SAVE), any(), eq(locale)))
                    .thenReturn("Erro ao salvar sinistro: Erro ao consultar apolice");
            when(messageSource.getMessage(eq(ClaimConstants.CLAIM_ERROR_SAVE_ROLLBACK), any(), eq(locale)))
                    .thenReturn("Rollback: Erro ao salvar sinistro: Erro ao consultar apolice");

            assertThatThrownBy(() -> claimServices.newClaimDto(
                    Collections.emptyList(), VALID_CLAIM_REQUEST_JSON, locale))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Rollback: Erro ao salvar sinistro: Erro ao consultar apolice");

            verify(claimRepository, never()).save(any(Claim.class));
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        }

        @Test
        @DisplayName("deve lançar BadRequestException quando o JSON de entrada é inválido")
        void shouldThrowBadRequestWhenInputJsonIsInvalid() {
            assertThatThrownBy(() -> claimServices.newClaimDto(
                    Collections.emptyList(), "{ json invalido ", locale))
                    .isInstanceOf(BadRequestException.class);

            verify(claimRepository, never()).save(any(Claim.class));
        }

        @Test
        @DisplayName("deve lançar BadRequestException quando a validação bean encontra violações")
        void shouldThrowBadRequestWhenBeanValidationFails() {
            jakarta.validation.ConstraintViolation<Object> violation =
                    org.mockito.Mockito.mock(jakarta.validation.ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("descricao obrigatoria");
            when(validator.validate(any())).thenReturn((Set) Set.of(violation));

            assertThatThrownBy(() -> claimServices.newClaimDto(
                    Collections.emptyList(), VALID_CLAIM_REQUEST_JSON, locale))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("descricao obrigatoria");

            verify(claimRepository, never()).save(any(Claim.class));
        }
    }

    @Nested
    @DisplayName("findByClaimNumberDto")
    class FindByClaimNumberDto {

        @Test
        @DisplayName("deve retornar o sinistro quando encontrado pelo número")
        void shouldReturnClaimWhenFound() {
            when(claimRepository.findByClaimNumber("SIN-001")).thenReturn(java.util.Optional.of(savedClaim));

            ClaimResponseDto response = claimServices.findByClaimNumberDto("SIN-001", locale);

            assertThat(response).isNotNull();
            assertThat(response.getClaimNumber()).isEqualTo("SIN-001");
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando o sinistro não existe")
        void shouldThrowNotFoundWhenClaimDoesNotExist() {
            when(claimRepository.findByClaimNumber("INEXISTENTE")).thenReturn(java.util.Optional.empty());
            when(messageSource.getMessage(eq(ClaimConstants.CLAIM_NOT_FOUND), any(), eq(locale)))
                    .thenReturn("Sinistro não encontrado");

            assertThatThrownBy(() -> claimServices.findByClaimNumberDto("INEXISTENTE", locale))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Sinistro não encontrado");
        }
    }

    @Nested
    @DisplayName("updateStatusDocOrReview")
    class UpdateStatusDocOrReview {

        @Test
        @DisplayName("deve atualizar status para DOCUMENTATION_PENDING com sucesso")
        void shouldUpdateStatusToDocumentationPending() {
            ClaimRequestStatusDto statusDto = ClaimRequestStatusDto.builder()
                    .status(ClaimStatus.DOCUMENTATION_PENDING.name())
                    .build();

            when(claimRepository.findByClaimNumber("SIN-001")).thenReturn(java.util.Optional.of(savedClaim));
            when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

            ClaimResponseDto response = claimServices.updateStatusDocOrReview("SIN-001", statusDto, locale);

            assertThat(response).isNotNull();
            verify(userServicesClients, times(1)).findByUserLogged();
            verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        }

        @Test
        @DisplayName("deve lançar BadRequestException ao tentar mover para status fora de DOCUMENTATION_PENDING/IN_REVIEW")
        void shouldThrowBadRequestForDisallowedStatus() {
            ClaimRequestStatusDto statusDto = ClaimRequestStatusDto.builder()
                    .status(ClaimStatus.APPROVED.name())
                    .build();

            when(claimRepository.findByClaimNumber("SIN-001")).thenReturn(java.util.Optional.of(savedClaim));

            assertThatThrownBy(() -> claimServices.updateStatusDocOrReview("SIN-001", statusDto, locale))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid Status: Update only DOCUMENTATION_PENDING or IN_REVIEW");

            verify(claimRepository, never()).save(any(Claim.class));
        }

        @Test
        @DisplayName("BUG: status inválido propaga IllegalArgumentException crua em vez de erro de negócio tratado")
        void shouldExposeUnhandledIllegalArgumentExceptionForInvalidStatus() {
            ClaimRequestStatusDto statusDto = ClaimRequestStatusDto.builder()
                    .status("STATUS_QUE_NAO_EXISTE")
                    .build();

            when(claimRepository.findByClaimNumber("SIN-001")).thenReturn(java.util.Optional.of(savedClaim));

            assertThatThrownBy(() -> claimServices.updateStatusDocOrReview("SIN-001", statusDto, locale))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(claimRepository, never()).save(any(Claim.class));
        }
    }

    @Nested
    @DisplayName("updateStatusApprovedOrDenied")
    class UpdateStatusApprovedOrDenied {

        @Test
        @DisplayName("deve atualizar para APPROVED sem a restrição de DOCUMENTATION_PENDING/IN_REVIEW")
        void shouldUpdateToApprovedWithoutStatusRestriction() {
            ClaimRequestStatusDto statusDto = ClaimRequestStatusDto.builder()
                    .status(ClaimStatus.APPROVED.name())
                    .build();

            when(claimRepository.findByClaimNumber("SIN-001")).thenReturn(java.util.Optional.of(savedClaim));
            when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

            ClaimResponseDto response = claimServices.updateStatusApprovedOrDenied("SIN-001", statusDto, locale);

            assertThat(response).isNotNull();
            verify(userServicesClients, times(1)).findByUserLogged();
        }

        @Test
        @DisplayName("deve lançar BadRequestException reembrulhado quando o sinistro não é encontrado")
        void shouldWrapNotFoundAsBadRequestWhenClaimMissing() {
            ClaimRequestStatusDto statusDto = ClaimRequestStatusDto.builder()
                    .status(ClaimStatus.DENIED.name())
                    .build();

            when(claimRepository.findByClaimNumber("INEXISTENTE")).thenReturn(java.util.Optional.empty());
            when(messageSource.getMessage(eq(ClaimConstants.CLAIM_NOT_FOUND), any(), eq(locale)))
                    .thenReturn("Sinistro não encontrado");

            assertThatThrownBy(() -> claimServices.updateStatusApprovedOrDenied("INEXISTENTE", statusDto, locale))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Sinistro não encontrado");
        }
    }
}