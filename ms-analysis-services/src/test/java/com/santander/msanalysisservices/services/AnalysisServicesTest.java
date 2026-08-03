package com.santander.msanalysisservices.services;

import com.santander.msanalysisservices.clients.ClaimServicesClients;
import com.santander.msanalysisservices.clients.UserServicesClients;
import com.santander.msanalysisservices.constants.AnalysisConstants;
import com.santander.msanalysisservices.dto.AnalysisRequestDto;
import com.santander.msanalysisservices.dto.AnalysisResponseDto;
import com.santander.msanalysisservices.dto.ClaimRequestStatusDto;
import com.santander.msanalysisservices.dto.UserResponseDto;
import com.santander.msanalysisservices.exception.BadRequestException;
import com.santander.msanalysisservices.exception.BusinessException;
import com.santander.msanalysisservices.exception.NotFoundException;
import com.santander.msanalysisservices.model.Analysis;
import com.santander.msanalysisservices.model.AnalysisResult;
import com.santander.msanalysisservices.repository.AnalysisRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServicesTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private UserServicesClients userServicesClients;

    @Mock
    private ClaimServicesClients claimServicesClients;

    @InjectMocks
    private AnalysisServices analysisServices;

    private UserResponseDto analystUser;
    private Analysis savedAnalysis;
    private AnalysisResponseDto analysisResponse;

    @BeforeEach
    void setUp() {
        analystUser = UserResponseDto.builder()
                .id(BigInteger.ONE)
                .role("ANALYST")
                .build();

        savedAnalysis = Analysis.builder()
                .claimId("SIN-001")
                .analystId(BigInteger.ONE)
                .result(AnalysisResult.APPROVED)
                .compensationAmount(new BigDecimal("1500.00"))
                .build();

        analysisResponse = AnalysisResponseDto.builder()
                .claimId("SIN-001")
                .result(AnalysisResult.APPROVED)
                .build();
    }

    private AnalysisRequestDto buildRequest(AnalysisResult result, String reasonForDenial) {
        return AnalysisRequestDto.builder()
                .claimId("SIN-001")
                .result(result)
                .reasonForDenial(reasonForDenial)
                .compensationAmount(new BigDecimal("1500.00"))
                .analysisDate("01/07/2026 10:30")
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("deve salvar a análise, atualizar status do sinistro e publicar no rabbit")
        void shouldSaveAnalysisSuccessfully() {
            AnalysisRequestDto request = buildRequest(AnalysisResult.APPROVED, null);

            when(userServicesClients.findByUserLogged()).thenReturn(analystUser);
            when(analysisRepository.existsByClaimId("SIN-001")).thenReturn(false);
            when(analysisRepository.save(any(Analysis.class))).thenReturn(savedAnalysis);
            when(modelMapper.map(savedAnalysis, AnalysisResponseDto.class)).thenReturn(analysisResponse);

            AnalysisResponseDto response = analysisServices.save(request);

            assertThat(response).isEqualTo(analysisResponse);

            verify(claimServicesClients, times(1)).updateStatus(
                    eq("SIN-001"),
                    eq(ClaimRequestStatusDto.builder().status(AnalysisResult.APPROVED.name()).build()));
            verify(rabbitTemplate, times(1)).convertAndSend(
                    eq(AnalysisConstants.RABBIT_QUEUE_ANALYSIS_EXCHANGE),
                    eq(AnalysisConstants.RABBIT_QUEUE_ANALYSIS_ROUTER),
                    eq(analysisResponse));
        }

        @Test
        @DisplayName("deve lançar BusinessException quando o usuário logado não é ANALYST")
        void shouldThrowBusinessExceptionWhenUserIsNotAnalyst() {
            AnalysisRequestDto request = buildRequest(AnalysisResult.APPROVED, null);
            UserResponseDto notAnalyst = UserResponseDto.builder().id(BigInteger.ONE).role("ADMIN").build();

            when(userServicesClients.findByUserLogged()).thenReturn(notAnalyst);

            assertThatThrownBy(() -> analysisServices.save(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Not permission");

            verify(analysisRepository, never()).save(any(Analysis.class));
        }

        @Test
        @DisplayName("deve lançar BadRequestException quando já existe análise para o claimId")
        void shouldThrowBadRequestWhenClaimIdAlreadyHasAnalysis() {
            AnalysisRequestDto request = buildRequest(AnalysisResult.APPROVED, null);

            when(userServicesClients.findByUserLogged()).thenReturn(analystUser);
            when(analysisRepository.existsByClaimId("SIN-001")).thenReturn(true);

            assertThatThrownBy(() -> analysisServices.save(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Claim already exists.");

            verify(analysisRepository, never()).save(any(Analysis.class));
        }

        @Test
        @DisplayName("BUG: análise DENIED sem motivo NÃO lança exceção (comportamento atual, incorreto)")
        void shouldNotThrow_currentBuggyBehavior_evenWithoutReasonOnDenied() {
            AnalysisRequestDto request = buildRequest(AnalysisResult.DENIED, null);

            when(userServicesClients.findByUserLogged()).thenReturn(analystUser);
            when(analysisRepository.existsByClaimId("SIN-001")).thenReturn(false);
            when(analysisRepository.save(any(Analysis.class))).thenReturn(savedAnalysis);
            when(modelMapper.map(savedAnalysis, AnalysisResponseDto.class)).thenReturn(analysisResponse);

            assertThatCode(() -> analysisServices.save(request)).doesNotThrowAnyException();

            verify(analysisRepository, times(1)).save(any(Analysis.class));
        }

        @Test
        @DisplayName("deve lançar BadRequestException quando analysisDate (não result!) for a string 'DENIED'")
        void shouldThrowBadRequestOnlyWhenAnalysisDateStringEqualsDenied() {
            AnalysisRequestDto request = AnalysisRequestDto.builder()
                    .claimId("SIN-001")
                    .result(AnalysisResult.APPROVED)
                    .reasonForDenial(null)
                    .compensationAmount(new BigDecimal("1500.00"))
                    .analysisDate("DENIED")
                    .build();

            when(userServicesClients.findByUserLogged()).thenReturn(analystUser);
            when(analysisRepository.existsByClaimId("SIN-001")).thenReturn(false);

            assertThatThrownBy(() -> analysisServices.save(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("When a request is denied, you must provide a reason.");

            verify(analysisRepository, never()).save(any(Analysis.class));
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("deve retornar todas as análises mapeadas para DTO")
        void shouldReturnAllAnalysesMapped() {
            when(analysisRepository.findAll()).thenReturn(List.of(savedAnalysis));
            when(modelMapper.map(savedAnalysis, AnalysisResponseDto.class)).thenReturn(analysisResponse);

            List<AnalysisResponseDto> result = analysisServices.findAll();

            assertThat(result).containsExactly(analysisResponse);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há análises")
        void shouldReturnEmptyListWhenNoAnalyses() {
            when(analysisRepository.findAll()).thenReturn(List.of());

            List<AnalysisResponseDto> result = analysisServices.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByClaimIdDto")
    class FindByClaimIdDto {

        @Test
        @DisplayName("deve retornar a análise quando encontrada pelo claimId")
        void shouldReturnAnalysisWhenFound() {
            when(analysisRepository.findByClaimId("SIN-001")).thenReturn(Optional.of(savedAnalysis));
            when(modelMapper.map(savedAnalysis, AnalysisResponseDto.class)).thenReturn(analysisResponse);

            AnalysisResponseDto result = analysisServices.findByClaimIdDto("SIN-001");

            assertThat(result).isEqualTo(analysisResponse);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando não há análise para o claimId")
        void shouldThrowNotFoundWhenAnalysisDoesNotExist() {
            when(analysisRepository.findByClaimId("INEXISTENTE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> analysisServices.findByClaimIdDto("INEXISTENTE"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Analysis not found");
        }
    }
}