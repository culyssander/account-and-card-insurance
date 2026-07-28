package com.santander.msclaimsquestionnaireservices.dto;

/**
 * Resposta polimórfica do endpoint /answer: ou devolve a próxima pergunta,
 * ou (quando o fluxo chega numa folha) devolve o resultado final.
 */
public record AnswerResponse(
        boolean completed,
        QuestionResponse nextQuestion,
        QuestionnaireResult result
) {
    public static AnswerResponse ofNextQuestion(QuestionResponse next) {
        return new AnswerResponse(false, next, null);
    }

    public static AnswerResponse ofResult(QuestionnaireResult result) {
        return new AnswerResponse(true, null, result);
    }
}
