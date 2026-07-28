package com.santander.msclaimsquestionnaireservices.flow;

import com.santander.msclaimsquestionnaireservices.exception.QuestionNotFoundException;
import com.santander.msclaimsquestionnaireservices.model.QuestionEntity;
import com.santander.msclaimsquestionnaireservices.repository.QuestionRepository;
import com.santander.msclaimsquestionnaireservices.model.QuestionOptionEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador da porta QuestionFlow. Cada chamada resolve um nó por
 * vez (via code, ex. "Q001") em vez de carregar a árvore inteira — a
 * pergunta atual é sempre conhecida pela QuestionnaireSession, então não há
 * motivo pra trazer nós que não serão usados nesta requisição.
 */
@Component
public class QuestionFlowImpl implements QuestionFlow {

    private static final String START_QUESTION_CODE = "Q001";

    private final QuestionRepository jpaRepository;

    public QuestionFlowImpl(QuestionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionNode start() {
        return nodeById(START_QUESTION_CODE);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionNode nodeById(String questionId) {
        QuestionEntity entity = jpaRepository.findByCodeFetchingOptions(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        return toDomain(entity);
    }

    private QuestionNode toDomain(QuestionEntity entity) {
        List<QuestionNode.QuestionOption> options = entity.getOptions().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new QuestionNode(entity.getCode(), entity.getText(), entity.getType(), options);
    }

    private QuestionNode.QuestionOption toDomain(QuestionOptionEntity option) {
        return new QuestionNode.QuestionOption(
                option.getOptionCode(),
                option.getLabel(),
                option.getNextQuestionCode(),
                option.getOutcomeCode()
        );
    }
}
