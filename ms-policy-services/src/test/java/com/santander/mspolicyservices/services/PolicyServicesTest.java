package com.santander.mspolicyservices.services;

import com.santander.mspolicyservices.clients.ProductsServicesClients;
import com.santander.mspolicyservices.clients.ProtocolSequencialClients;
import com.santander.mspolicyservices.clients.UserServicesClients;
import com.santander.mspolicyservices.constants.PolicyConstants;
import com.santander.mspolicyservices.dto.PolicyRequestDto;
import com.santander.mspolicyservices.dto.PolicyResponseDto;
import com.santander.mspolicyservices.dto.ProductResponseDto;
import com.santander.mspolicyservices.dto.UserResponseDto;
import com.santander.mspolicyservices.exception.AccessDeniedException;
import com.santander.mspolicyservices.exception.BusinessException;
import com.santander.mspolicyservices.exception.NotFoundException;
import com.santander.mspolicyservices.model.Policy;
import com.santander.mspolicyservices.model.PolicyStatus;
import com.santander.mspolicyservices.repository.PolicyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServicesTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ProductsServicesClients productsServicesClients;

    @Mock
    private ProtocolSequencialClients protocolSequencialClients;

    @Mock
    private UserServicesClients userServicesClients;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private PolicyServices policyServices;

    private final Locale locale = new Locale("pt", "BR");

    private UserResponseDto insuredUser;
    private ProductResponseDto product;
    private Policy policy;

    @BeforeEach
    void setUp() {
        insuredUser = UserResponseDto.builder()
                .cpf("12345678900")
                .role("INSURED")
                .build();

        product = ProductResponseDto.builder()
                .id(BigInteger.ONE)
                .code("PROD-001")
                .build();

        policy = Policy.builder()
                .policyNumber("PROTOCOL-001")
                .productId(product.getId())
                .productCode(product.getCode())
                .cpf(insuredUser.getCpf())
                .status(PolicyStatus.DRAFT.name())
                .startDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createPolicy")
    class CreatePolicy {

        @Test
        @DisplayName("deve criar apólice com sucesso quando usuário e produto são válidos")
        void shouldCreatePolicySuccessfully() {
            PolicyRequestDto request = PolicyRequestDto.builder()
                    .productCode("PROD-001")
                    .build();

            when(userServicesClients.findByUserLogged()).thenReturn(insuredUser);
            when(productsServicesClients.findProductsByCode("PROD-001")).thenReturn(product);
            when(protocolSequencialClients.generateProtocol("PROD-001")).thenReturn("PROTOCOL-001");
            when(policyRepository.save(any(Policy.class))).thenReturn(policy);

            PolicyResponseDto response = policyServices.createPolicy(request, locale);

            assertThat(response).isNotNull();
            assertThat(response.getPolicyNumber()).isEqualTo("PROTOCOL-001");
            assertThat(response.getCpf()).isEqualTo(insuredUser.getCpf());
            assertThat(response.getStatus()).isEqualTo(PolicyStatus.DRAFT.name());

            verify(policyRepository, times(1)).save(any(Policy.class));
        }

        @Test
        @DisplayName("deve lançar AccessDeniedException quando role do usuário não é INSURED")
        void shouldThrowAccessDeniedWhenRoleIsNotInsured() {
            PolicyRequestDto request = PolicyRequestDto.builder()
                    .productCode("PROD-001")
                    .build();

            UserResponseDto adminUser = UserResponseDto.builder()
                    .cpf("00000000000")
                    .role("ADMIN")
                    .build();

            when(userServicesClients.findByUserLogged()).thenReturn(adminUser);
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_ACCESS_DENIED), any(), eq(locale)))
                    .thenReturn("Acesso negado");

            assertThatThrownBy(() -> policyServices.createPolicy(request, locale))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Acesso negado");

            verify(policyRepository, never()).save(any(Policy.class));
        }

        @Test
        @DisplayName("deve lançar AccessDeniedException quando client de usuário nega o acesso")
        void shouldThrowAccessDeniedWhenUserClientDenies() {
            PolicyRequestDto request = PolicyRequestDto.builder()
                    .productCode("PROD-001")
                    .build();

            when(userServicesClients.findByUserLogged()).thenThrow(new AccessDeniedException("denied"));
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_ACCESS_DENIED), any(), eq(locale)))
                    .thenReturn("Acesso negado");

            assertThatThrownBy(() -> policyServices.createPolicy(request, locale))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Acesso negado");

            verify(policyRepository, never()).save(any(Policy.class));
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando produto não é encontrado")
        void shouldThrowNotFoundWhenProductIsNull() {
            PolicyRequestDto request = PolicyRequestDto.builder()
                    .productCode("PROD-999")
                    .build();

            when(userServicesClients.findByUserLogged()).thenReturn(insuredUser);
            when(productsServicesClients.findProductsByCode("PROD-999")).thenReturn(null);
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_NOT_FOUND), any(), eq(locale)))
                    .thenReturn("Produto não encontrado");

            assertThatThrownBy(() -> policyServices.createPolicy(request, locale))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Produto não encontrado");

            verify(policyRepository, never()).save(any(Policy.class));
        }

        @Test
        @DisplayName("deve propagar BusinessException quando client de produtos falha")
        void shouldPropagateBusinessExceptionFromProductClient() {
            PolicyRequestDto request = PolicyRequestDto.builder()
                    .productCode("PROD-001")
                    .build();

            when(userServicesClients.findByUserLogged()).thenReturn(insuredUser);
            when(productsServicesClients.findProductsByCode("PROD-001"))
                    .thenThrow(new BusinessException("erro no serviço de produtos"));

            assertThatThrownBy(() -> policyServices.createPolicy(request, locale))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("erro no serviço de produtos");

            verify(policyRepository, never()).save(any(Policy.class));
        }
    }

    @Nested
    @DisplayName("findByPolicyNumber")
    class FindByPolicyNumber {

        @Test
        @DisplayName("deve retornar a apólice quando encontrada pelo número")
        void shouldReturnPolicyWhenFound() {
            when(policyRepository.findByPolicyNumber("PROTOCOL-001")).thenReturn(Optional.of(policy));

            Policy result = policyServices.findByPolicyNumber("PROTOCOL-001", locale);

            assertThat(result).isEqualTo(policy);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando apólice não existe")
        void shouldThrowNotFoundWhenPolicyDoesNotExist() {
            when(policyRepository.findByPolicyNumber("INEXISTENTE")).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_NOT_FOUND), any(), eq(locale)))
                    .thenReturn("Apólice não encontrada");

            assertThatThrownBy(() -> policyServices.findByPolicyNumber("INEXISTENTE", locale))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Apólice não encontrada");
        }
    }

    @Nested
    @DisplayName("findByCPFAndPolicyNumber")
    class FindByCpfAndPolicyNumber {

        @Test
        @DisplayName("deve retornar a apólice quando CPF e número conferem")
        void shouldReturnPolicyWhenCpfAndNumberMatch() {
            when(policyRepository.findByCpfAndPolicyNumber("12345678900", "PROTOCOL-001"))
                    .thenReturn(Optional.of(policy));

            Policy result = policyServices.findByCPFAndPolicyNumber("12345678900", "PROTOCOL-001", locale);

            assertThat(result).isEqualTo(policy);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando CPF e número não conferem")
        void shouldThrowNotFoundWhenNoMatch() {
            when(policyRepository.findByCpfAndPolicyNumber("00000000000", "PROTOCOL-001"))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_NOT_FOUND), any(), eq(locale)))
                    .thenReturn("Apólice não encontrada");

            assertThatThrownBy(() ->
                    policyServices.findByCPFAndPolicyNumber("00000000000", "PROTOCOL-001", locale))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Apólice não encontrada");
        }
    }

    @Nested
    @DisplayName("findByAdminOrAnalysis")
    class FindByAdminOrAnalysis {

        @Test
        @DisplayName("deve retornar os dados da apólice sem validar role do usuário")
        void shouldReturnPolicyDataWithoutRoleCheck() {
            when(policyRepository.findByPolicyNumber("PROTOCOL-001")).thenReturn(Optional.of(policy));
            when(userServicesClients.findByInsuredLogged()).thenReturn(insuredUser);
            when(productsServicesClients.findProductsByCode("PROD-001")).thenReturn(product);

            PolicyResponseDto response = policyServices.findByAdminOrAnalysis("PROTOCOL-001", locale);

            assertThat(response).isNotNull();
            assertThat(response.getPolicyNumber()).isEqualTo("PROTOCOL-001");
            verify(userServicesClients, never()).findByUserLogged();
        }
    }

    @Nested
    @DisplayName("findByInsured")
    class FindByInsured {

        @Test
        @DisplayName("deve retornar a apólice do segurado logado")
        void shouldReturnPolicyForLoggedInsured() {
            when(userServicesClients.findByUserLogged()).thenReturn(insuredUser);
            when(policyRepository.findByCpfAndPolicyNumber(insuredUser.getCpf(), "PROTOCOL-001"))
                    .thenReturn(Optional.of(policy));
            when(userServicesClients.findByInsuredLogged()).thenReturn(insuredUser);
            when(productsServicesClients.findProductsByCode("PROD-001")).thenReturn(product);

            PolicyResponseDto response = policyServices.findByInsured("PROTOCOL-001", locale);

            assertThat(response).isNotNull();
            assertThat(response.getCpf()).isEqualTo(insuredUser.getCpf());
        }

        @Test
        @DisplayName("deve lançar AccessDeniedException quando usuário logado não é INSURED")
        void shouldThrowAccessDeniedWhenUserIsNotInsured() {
            UserResponseDto adminUser = UserResponseDto.builder()
                    .cpf("00000000000")
                    .role("ADMIN")
                    .build();

            when(userServicesClients.findByUserLogged()).thenReturn(adminUser);
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_ACCESS_DENIED), any(), eq(locale)))
                    .thenReturn("Acesso negado");

            assertThatThrownBy(() -> policyServices.findByInsured("PROTOCOL-001", locale))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Acesso negado");

            verify(policyRepository, never()).findByCpfAndPolicyNumber(anyString(), anyString());
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando apólice não pertence ao CPF do segurado")
        void shouldThrowNotFoundWhenPolicyDoesNotBelongToInsured() {
            when(userServicesClients.findByUserLogged()).thenReturn(insuredUser);
            when(policyRepository.findByCpfAndPolicyNumber(insuredUser.getCpf(), "PROTOCOL-999"))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(PolicyConstants.POLICY_NOT_FOUND), any(), eq(locale)))
                    .thenReturn("Apólice não encontrada");

            assertThatThrownBy(() -> policyServices.findByInsured("PROTOCOL-999", locale))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Apólice não encontrada");
        }
    }
}