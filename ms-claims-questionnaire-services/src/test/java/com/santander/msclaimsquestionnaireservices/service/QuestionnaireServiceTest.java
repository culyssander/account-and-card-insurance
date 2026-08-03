package com.santander.msclaimsquestionnaireservices.service;

import com.santander.msclaimsquestionnaireservices.clients.ClaimServicesClients;
import com.santander.msclaimsquestionnaireservices.dto.AnswerRequest;
import com.santander.msclaimsquestionnaireservices.dto.AnswerResponse;
import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import com.santander.msclaimsquestionnaireservices.exception.BusinessException;
import com.santander.msclaimsquestionnaireservices.exception.QuestionMismatchException;
import com.santander.msclaimsquestionnaireservices.exception.QuestionNotFoundException;
import com.santander.msclaimsquestionnaireservices.exception.QuestionnaireSessionNotFoundException;
import com.santander.msclaimsquestionnaireservices.flow.QuestionFlow;
import com.santander.msclaimsquestionnaireservices.flow.QuestionNode;
import com.santander.msclaimsquestionnaireservices.model.AnsweredQuestion;
import com.santander.msclaimsquestionnaireservices.model.QuestionnaireSession;
import com.santander.msclaimsquestionnaireservices.repository.AnsweredQuestionRepository;
import com.santander.msclaimsquestionnaireservices.repository.QuestionnaireSessionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    private static final String CLAIM_ID = "claim-123";
    private static final String FIRST_QUESTION_ID = "Q1";
    private static final String SECOND_QUESTION_ID = "Q2";
    private static final String SELECTED_OPTION_ID = "OPT1";

    @Mock
    private QuestionFlow flow;
    @Mock
    private QuestionnaireSessionRepository sessionStore;
    @Mock
    private AnsweredQuestionRepository answeredQuestionRepository;
    @Mock
    private ClaimServicesClients servicesClients;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private QuestionNode firstQuestionNode;
    @Mock
    private QuestionNode secondQuestionNode;
    @Mock
    private QuestionNode.QuestionOption nonTerminalOption;
    @Mock
    private QuestionNode.QuestionOption terminalOption;
    @Mock
    private ClaimResponseDto claimResponseDto;

    @InjectMocks
    private QuestionnaireService questionnaireService;

    @Nested
    class Start {

        @Test
        void start_whenClaimIsValid_createsSessionAndReturnsFirstQuestion() {
            when(servicesClients.findClaimById(CLAIM_ID)).thenReturn(claimResponseDto);
            when(flow.start()).thenReturn(firstQuestionNode);
            when(firstQuestionNode.id()).thenReturn(FIRST_QUESTION_ID);
            when(firstQuestionNode.options()).thenReturn(List.of());
            when(sessionStore.existsByClaimIdAndCurrentQuestionId(CLAIM_ID, FIRST_QUESTION_ID))
                    .thenReturn(false);

            var response = questionnaireService.start(CLAIM_ID);

            assertThat(response.questionId()).isEqualTo(FIRST_QUESTION_ID);
            verify(sessionStore).save(any(QuestionnaireSession.class));
        }

        @Test
        void start_whenSessionAlreadyExists_doesNotCreateDuplicateSession() {
            when(servicesClients.findClaimById(CLAIM_ID)).thenReturn(claimResponseDto);
            when(flow.start()).thenReturn(firstQuestionNode);
            when(firstQuestionNode.id()).thenReturn(FIRST_QUESTION_ID);
            when(firstQuestionNode.options()).thenReturn(List.of());
            when(sessionStore.existsByClaimIdAndCurrentQuestionId(CLAIM_ID, FIRST_QUESTION_ID))
                    .thenReturn(true);

            questionnaireService.start(CLAIM_ID);

            verify(sessionStore, never()).save(any(QuestionnaireSession.class));
        }

        @Test
        void start_whenClaimNotFound_throwsQuestionNotFoundException() {
            when(servicesClients.findClaimById(CLAIM_ID)).thenReturn(null);

            assertThatThrownBy(() -> questionnaireService.start(CLAIM_ID))
                    .isInstanceOf(QuestionNotFoundException.class);

            verify(sessionStore, never()).save(any());
        }
    }

    @Nested
    class Answer {

        @Test
        void answer_whenSessionNotFound_throwsQuestionnaireSessionNotFoundException() {
            AnswerRequest request = new AnswerRequest(FIRST_QUESTION_ID, SELECTED_OPTION_ID);
            when(sessionStore.findByClaimIdAndCurrentQuestionId(CLAIM_ID, FIRST_QUESTION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionnaireService.answer(CLAIM_ID, request))
                    .isInstanceOf(QuestionnaireSessionNotFoundException.class);

            verify(answeredQuestionRepository, never()).save(any());
        }

        @Test
        void answer_whenRequestedQuestionDoesNotMatchSessionQuestion_throwsQuestionMismatchException() {
            AnswerRequest request = new AnswerRequest(SECOND_QUESTION_ID, SELECTED_OPTION_ID);
            QuestionnaireSession session = QuestionnaireSession.builder()
                    .claimId(CLAIM_ID)
                    .currentQuestionId(FIRST_QUESTION_ID)
                    .build();
            when(sessionStore.findByClaimIdAndCurrentQuestionId(CLAIM_ID, SECOND_QUESTION_ID))
                    .thenReturn(Optional.of(session));

            assertThatThrownBy(() -> questionnaireService.answer(CLAIM_ID, request))
                    .isInstanceOf(QuestionMismatchException.class);
        }

        @Test
        void answer_whenTerminal_savesAnswerPublishesToRabbitAndReturnsResult() {
            AnswerRequest request = new AnswerRequest(FIRST_QUESTION_ID, SELECTED_OPTION_ID);
            QuestionnaireSession session = QuestionnaireSession.builder()
                    .claimId(CLAIM_ID)
                    .currentQuestionId(FIRST_QUESTION_ID)
                    .build();

            when(sessionStore.findByClaimIdAndCurrentQuestionId(CLAIM_ID, FIRST_QUESTION_ID))
                    .thenReturn(Optional.of(session));
            when(flow.nodeById(FIRST_QUESTION_ID)).thenReturn(firstQuestionNode);
            when(firstQuestionNode.optionById(SELECTED_OPTION_ID)).thenReturn(terminalOption);
            when(terminalOption.isTerminal()).thenReturn(true);
            when(terminalOption.outcomeCode()).thenReturn("APPROVED");
            when(answeredQuestionRepository.findAllBySessionClaimId(CLAIM_ID)).thenReturn(List.of());

            AnswerResponse response = questionnaireService.answer(CLAIM_ID, request);

            assertThat(response).isNotNull();
            verify(answeredQuestionRepository).save(any(AnsweredQuestion.class));
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(response));
        }

        @Test
        void answer_whenTerminal_andRabbitPublishFails_propagatesBusinessException() {
            AnswerRequest request = new AnswerRequest(FIRST_QUESTION_ID, SELECTED_OPTION_ID);
            QuestionnaireSession session = QuestionnaireSession.builder()
                    .claimId(CLAIM_ID)
                    .currentQuestionId(FIRST_QUESTION_ID)
                    .build();

            when(sessionStore.findByClaimIdAndCurrentQuestionId(CLAIM_ID, FIRST_QUESTION_ID))
                    .thenReturn(Optional.of(session));
            when(flow.nodeById(FIRST_QUESTION_ID)).thenReturn(firstQuestionNode);
            when(firstQuestionNode.optionById(SELECTED_OPTION_ID)).thenReturn(terminalOption);
            when(terminalOption.isTerminal()).thenReturn(true);
            when(terminalOption.outcomeCode()).thenReturn("APPROVED");
            when(answeredQuestionRepository.findAllBySessionClaimId(CLAIM_ID)).thenReturn(List.of());
            doThrow(new BusinessException("broker indisponível"))
                    .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AnswerResponse.class));

            assertThatThrownBy(() -> questionnaireService.answer(CLAIM_ID, request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class FindQuestionnaireByClaimIdDto {

        @Test
        void findQuestionnaireByClaimIdDto_returnsClaimWithAllAnsweredQuestions() {
            when(servicesClients.findClaimById(CLAIM_ID)).thenReturn(claimResponseDto);
            when(answeredQuestionRepository.findAllBySessionClaimId(CLAIM_ID)).thenReturn(List.of());

            ClaimResponseDto result = questionnaireService.findQuestionnaireByClaimIdDto(CLAIM_ID);

            assertThat(result).isEqualTo(claimResponseDto);
            verify(claimResponseDto).setAllAnsweredQuestion(List.of());
        }
    }
}