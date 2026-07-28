package com.santander.msclaimsquestionnaireservices.dto;

import java.util.List;

/**
 * Resultado final do questionário. outcomeCode é a decisão de roteamento
 * da folha (ex.: REQUEST_DOCUMENTS_CARD_THEFT, EVALUATE_POLICY_COVERAGE) —
 * não é a cobertura resolvida. A resolução de cobertura e a análise
 * (aprovada/negada) acontecem depois, no Sinistro Service, com base nesse
 * código e nos anexos enviados pelo cliente.
 */
public record QuestionnaireResult(
        String claimId,
        String outcomeCode,
        List<AnsweredQuestionDto> answers
) {
}
