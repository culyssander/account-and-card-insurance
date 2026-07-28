package com.santander.msclaimsquestionnaireservices.service;

import com.santander.msclaimsquestionnaireservices.clients.ClaimServicesClients;
import com.santander.msclaimsquestionnaireservices.dto.AnswerRequest;
import com.santander.msclaimsquestionnaireservices.dto.AnswerResponse;
import com.santander.msclaimsquestionnaireservices.dto.AnsweredQuestionDto;
import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import com.santander.msclaimsquestionnaireservices.dto.OptionResponse;
import com.santander.msclaimsquestionnaireservices.dto.QuestionResponse;
import com.santander.msclaimsquestionnaireservices.dto.QuestionnaireResult;
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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuestionnaireService {

    private final QuestionFlow flow;
    private final QuestionnaireSessionRepository sessionStore;
    private final AnsweredQuestionRepository answeredQuestionRepository;
    private final ClaimServicesClients servicesClients;

    public QuestionResponse start(String claimId) {
        validateClaimId(claimId);
        QuestionNode firstQuestion = flow.start();
        String questionId = firstQuestion.id();

        if (!sessionStore.existsByClaimIdAndCurrentQuestionId(claimId, questionId)) {
            QuestionnaireSession session = QuestionnaireSession.builder()
                    .claimId(claimId)
                    .currentQuestionId(questionId)
                    .build();
            sessionStore.save(session);
        }

        return toResponse(firstQuestion);
    }

    public AnswerResponse answer(AnswerRequest request) {
        String claimId = request.claimId();
        String questionId = request.questionId();

        QuestionnaireSession session = sessionStore.findByClaimIdAndCurrentQuestionId(claimId, questionId)
                .orElseThrow(() -> new QuestionnaireSessionNotFoundException(claimId, questionId));

        if (!session.getCurrentQuestionId().equals(request.questionId())) {
            throw new QuestionMismatchException(session.getCurrentQuestionId(), request.questionId());
        }

        QuestionNode currentQuestion = flow.nodeById(request.questionId());
        QuestionNode.QuestionOption selected = currentQuestion.optionById(request.selectedOption());

        AnsweredQuestion answeredQuestion = AnsweredQuestion.builder()
                .questionnaireId(session.getId())
//                .claimId(claimId)
                .session(session)
                .questionId(questionId)
                .answer(request.selectedOption())
                .createdAt(LocalDateTime.now())
                .build();

        if (selected.isTerminal()) {
            answeredQuestionRepository.save(answeredQuestion);
            List<AnsweredQuestionDto> allByClaimId = answeredQuestionRepository.findAllBySessionClaimId(claimId)
                    .stream().map(this::toResponseAnswer).toList();
            return AnswerResponse.ofResult(
                    new QuestionnaireResult(claimId, selected.outcomeCode(), allByClaimId));
        }

        answeredQuestion.setNextQuestionId(selected.nextQuestionId());
        sessionStore.save(session);

        QuestionNode nextQuestion = flow.nodeById(selected.nextQuestionId());
        return AnswerResponse.ofNextQuestion(toResponse(nextQuestion));
    }

    private QuestionResponse toResponse(QuestionNode node) {
        List<OptionResponse> options = node.options().stream()
                .map(o -> new OptionResponse(o.id(), o.label()))
                .collect(Collectors.toList());
        return new QuestionResponse(node.id(), node.question(), node.type(), options);
    }

    private AnsweredQuestionDto toResponseAnswer(AnsweredQuestion answered) {
        return new AnsweredQuestionDto(answered.getQuestionId(), answered.getAnswer());
    }

    private ClaimResponseDto findByClaimId(String claimId) {
        try {
            return servicesClients.findClaimById(claimId);
        } catch (BusinessException e) {
            throw new BusinessException("Error to find Claim by Id " + e.getMessage());
        }
    }

    private void validateClaimId(String claimId) {
        ClaimResponseDto claimResponseDto = findByClaimId(claimId);

        if (Objects.isNull(claimResponseDto)) {
            throw new QuestionNotFoundException("Claim not found");
        }
    }
}
