package com.santander.msclaimsquestionnaireservices.service;

import com.santander.msclaimsquestionnaireservices.clients.ClaimServicesClients;
import com.santander.msclaimsquestionnaireservices.dto.AnswerRequest;
import com.santander.msclaimsquestionnaireservices.dto.AnswerResponse;
import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import com.santander.msclaimsquestionnaireservices.flow.QuestionFlow;
import com.santander.msclaimsquestionnaireservices.flow.QuestionNode;
import com.santander.msclaimsquestionnaireservices.model.QuestionType;
import com.santander.msclaimsquestionnaireservices.model.QuestionnaireSession;
import com.santander.msclaimsquestionnaireservices.repository.AnsweredQuestionRepository;
import com.santander.msclaimsquestionnaireservices.repository.QuestionnaireSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

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


    private QuestionnaireService service;


    @BeforeEach
    void setup() {
        service = new QuestionnaireService(
                flow,
                sessionStore,
                answeredQuestionRepository,
                servicesClients,
                rabbitTemplate
        );
    }


    @Test
    void shouldStartQuestionnaire() {
        QuestionNode node = mock(QuestionNode.class);

        when(node.id()).thenReturn("Q1");
        when(node.question()).thenReturn("Qual tipo de sinistro?");
        when(node.type()).thenReturn(QuestionType.valueOf("SINGLE_CHOICE"));
        when(flow.start()).thenReturn(node);
        when(sessionStore.existsByClaimIdAndCurrentQuestionId("123", "Q1"))
                .thenReturn(false);
        when(servicesClients.findClaimById(anyString())).thenReturn(new ClaimResponseDto());

        var response = service.start("123");

        assertThat(response).isNotNull();
        verify(sessionStore).save(any(QuestionnaireSession.class));
    }


    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        AnswerRequest request = new AnswerRequest("Q1", "YES");
        when(sessionStore.findByClaimIdAndCurrentQuestionId("123","Q1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.answer("123", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFinishQuestionnaireWhenSelectedOptionIsTerminal() {

        AnswerRequest request =
                new AnswerRequest("Q1", "YES");


        QuestionnaireSession session =
                QuestionnaireSession.builder()
                        .id(BigInteger.ONE)
                        .claimId("123")
                        .currentQuestionId("Q1")
                        .build();


        when(sessionStore.findByClaimIdAndCurrentQuestionId(
                "123",
                "Q1"
        ))
                .thenReturn(Optional.of(session));


        QuestionNode node = mock(QuestionNode.class);
        QuestionNode.QuestionOption option =
                mock(QuestionNode.QuestionOption.class);


        when(flow.nodeById("Q1"))
                .thenReturn(node);

        when(node.optionById("YES"))
                .thenReturn(option);

        when(option.isTerminal())
                .thenReturn(true);

        when(option.outcomeCode())
                .thenReturn("APPROVED");


        when(answeredQuestionRepository.findAllBySessionClaimId("123"))
                .thenReturn(List.of());


        AnswerResponse response =
                service.answer("123", request);


        assertThat(response)
                .isNotNull();


        verify(answeredQuestionRepository)
                .save(any());


        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        Optional.ofNullable(any())
                );
    }

}
