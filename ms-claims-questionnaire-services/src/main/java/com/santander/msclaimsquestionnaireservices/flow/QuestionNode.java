package com.santander.msclaimsquestionnaireservices.flow;


import com.santander.msclaimsquestionnaireservices.exception.InvalidOptionException;
import com.santander.msclaimsquestionnaireservices.model.QuestionType;

import java.util.List;

/**
 * Nó do fluxo de perguntas — modelo de domínio, agnóstico de persistência.
 * Cada QuestionOption é ou uma transição (nextQuestionId) ou uma folha do
 * questionário (outcomeCode). A folha não resolve a cobertura final — ela
 * decide o roteamento (pedir documentos, avaliar apólice, verificar bens)
 * que o Sinistro Service usa para abrir a análise. Validado no construtor
 * compacto pra falhar cedo, na subida do contexto.
 */
public record QuestionNode(
        String id,
        String question,
        QuestionType type,
        List<QuestionOption> options
) {

    public record QuestionOption(String id, String label, String nextQuestionId, String outcomeCode) {

        public QuestionOption {
            boolean hasNext = nextQuestionId != null;
            boolean hasOutcome = outcomeCode != null;
            if (hasNext == hasOutcome) {
                throw new IllegalStateException(
                        "Opção '" + id + "' deve ter exatamente um entre nextQuestionId e outcomeCode");
            }
        }

        public boolean isTerminal() {
            return outcomeCode != null;
        }
    }

    public QuestionOption optionById(String optionId) {
        return options.stream()
                .filter(o -> o.id().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new InvalidOptionException(id, optionId));
    }
}
