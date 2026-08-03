package com.santander.msclaimsquestionnaireservices.flow;

import com.santander.msclaimsquestionnaireservices.exception.QuestionNotFoundException;
import com.santander.msclaimsquestionnaireservices.model.QuestionEntity;
import com.santander.msclaimsquestionnaireservices.model.QuestionOptionEntity;
import com.santander.msclaimsquestionnaireservices.model.QuestionType;
import com.santander.msclaimsquestionnaireservices.repository.QuestionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionFlowImplTest {

    private static final String START_QUESTION_CODE = "Q001";
    private static final String QUESTION_CODE = "Q002";
    private static final String QUESTION_TEXT = "O cartão foi furtado ou roubado?";
    private static final String OPTION_CODE = "OPT1";
    private static final String OPTION_LABEL = "Sim";
    private static final String NEXT_QUESTION_CODE = "Q003";
    private static final String OUTCOME_CODE = "REQUEST_DOCUMENTS_CARD_THEFT";

    @Mock
    private QuestionRepository jpaRepository;

    @Mock
    private QuestionEntity questionEntity;

    @Mock
    private QuestionOptionEntity questionOptionEntity;

    @Mock
    private QuestionType questionType;

    private QuestionFlowImpl questionFlow;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        questionFlow = new QuestionFlowImpl(jpaRepository);
    }

    @Nested
    class Start {

        @Test
        void start_delegatesToNodeByIdWithStartQuestionCode() {
            when(jpaRepository.findByCodeFetchingOptions(START_QUESTION_CODE))
                    .thenReturn(Optional.of(questionEntity));
            when(questionEntity.getCode()).thenReturn(START_QUESTION_CODE);
            when(questionEntity.getText()).thenReturn(QUESTION_TEXT);
            when(questionEntity.getType()).thenReturn(questionType);
            when(questionEntity.getOptions()).thenReturn(List.of());

            QuestionNode result = questionFlow.start();

            assertThat(result.id()).isEqualTo(START_QUESTION_CODE);
            verify(jpaRepository).findByCodeFetchingOptions(START_QUESTION_CODE);
        }

        @Test
        void start_whenStartQuestionNotFound_throwsQuestionNotFoundException() {
            when(jpaRepository.findByCodeFetchingOptions(START_QUESTION_CODE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionFlow.start())
                    .isInstanceOf(QuestionNotFoundException.class);
        }
    }

    @Nested
    class NodeById {

        @Test
        void nodeById_whenFound_mapsEntityFieldsToQuestionNode() {
            when(jpaRepository.findByCodeFetchingOptions(QUESTION_CODE))
                    .thenReturn(Optional.of(questionEntity));
            when(questionEntity.getCode()).thenReturn(QUESTION_CODE);
            when(questionEntity.getText()).thenReturn(QUESTION_TEXT);
            when(questionEntity.getType()).thenReturn(questionType);
            when(questionEntity.getOptions()).thenReturn(List.of());

            QuestionNode result = questionFlow.nodeById(QUESTION_CODE);

            assertThat(result.id()).isEqualTo(QUESTION_CODE);
            assertThat(result.question()).isEqualTo(QUESTION_TEXT);
            assertThat(result.type()).isEqualTo(questionType);
            assertThat(result.options()).isEmpty();
        }

        @Test
        void nodeById_whenFound_mapsNonTerminalOptionFieldsToQuestionOption() {
            when(jpaRepository.findByCodeFetchingOptions(QUESTION_CODE))
                    .thenReturn(Optional.of(questionEntity));
            when(questionEntity.getCode()).thenReturn(QUESTION_CODE);
            when(questionEntity.getText()).thenReturn(QUESTION_TEXT);
            when(questionEntity.getType()).thenReturn(questionType);
            when(questionEntity.getOptions()).thenReturn(List.of(questionOptionEntity));

            when(questionOptionEntity.getOptionCode()).thenReturn(OPTION_CODE);
            when(questionOptionEntity.getLabel()).thenReturn(OPTION_LABEL);
            when(questionOptionEntity.getNextQuestionCode()).thenReturn(NEXT_QUESTION_CODE);
            when(questionOptionEntity.getOutcomeCode()).thenReturn(null);

            QuestionNode result = questionFlow.nodeById(QUESTION_CODE);

            assertThat(result.options()).hasSize(1);
            QuestionNode.QuestionOption option = result.options().get(0);
            assertThat(option.id()).isEqualTo(OPTION_CODE);
            assertThat(option.label()).isEqualTo(OPTION_LABEL);
            assertThat(option.nextQuestionId()).isEqualTo(NEXT_QUESTION_CODE);
            assertThat(option.outcomeCode()).isNull();
            assertThat(option.isTerminal()).isFalse();
        }

        @Test
        void nodeById_whenFound_mapsTerminalOptionFieldsToQuestionOption() {
            when(jpaRepository.findByCodeFetchingOptions(QUESTION_CODE))
                    .thenReturn(Optional.of(questionEntity));
            when(questionEntity.getCode()).thenReturn(QUESTION_CODE);
            when(questionEntity.getText()).thenReturn(QUESTION_TEXT);
            when(questionEntity.getType()).thenReturn(questionType);
            when(questionEntity.getOptions()).thenReturn(List.of(questionOptionEntity));

            when(questionOptionEntity.getOptionCode()).thenReturn(OPTION_CODE);
            when(questionOptionEntity.getLabel()).thenReturn(OPTION_LABEL);
            when(questionOptionEntity.getNextQuestionCode()).thenReturn(null);
            when(questionOptionEntity.getOutcomeCode()).thenReturn(OUTCOME_CODE);

            QuestionNode result = questionFlow.nodeById(QUESTION_CODE);

            assertThat(result.options()).hasSize(1);
            QuestionNode.QuestionOption option = result.options().get(0);
            assertThat(option.id()).isEqualTo(OPTION_CODE);
            assertThat(option.label()).isEqualTo(OPTION_LABEL);
            assertThat(option.nextQuestionId()).isNull();
            assertThat(option.outcomeCode()).isEqualTo(OUTCOME_CODE);
            assertThat(option.isTerminal()).isTrue();
        }

        @Test
        void nodeById_whenOptionHasBothNextQuestionAndOutcome_propagatesIllegalStateException() {
            when(jpaRepository.findByCodeFetchingOptions(QUESTION_CODE))
                    .thenReturn(Optional.of(questionEntity));
            when(questionEntity.getOptions()).thenReturn(List.of(questionOptionEntity));

            when(questionOptionEntity.getOptionCode()).thenReturn(OPTION_CODE);
            when(questionOptionEntity.getLabel()).thenReturn(OPTION_LABEL);
            when(questionOptionEntity.getNextQuestionCode()).thenReturn(NEXT_QUESTION_CODE);
            when(questionOptionEntity.getOutcomeCode()).thenReturn(OUTCOME_CODE);

            assertThatThrownBy(() -> questionFlow.nodeById(QUESTION_CODE))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void nodeById_whenQuestionNotFound_throwsQuestionNotFoundExceptionWithGivenId() {
            when(jpaRepository.findByCodeFetchingOptions(QUESTION_CODE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionFlow.nodeById(QUESTION_CODE))
                    .isInstanceOf(QuestionNotFoundException.class);
        }

        @Test
        void nodeById_whenQuestionNotFound_doesNotAttemptToMapOptions() {
            when(jpaRepository.findByCodeFetchingOptions(QUESTION_CODE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionFlow.nodeById(QUESTION_CODE))
                    .isInstanceOf(QuestionNotFoundException.class);

            verify(questionEntity, never()).getOptions();
        }
    }
}