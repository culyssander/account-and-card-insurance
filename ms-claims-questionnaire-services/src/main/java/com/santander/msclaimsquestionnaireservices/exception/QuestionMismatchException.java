package com.santander.msclaimsquestionnaireservices.exception;

public class QuestionMismatchException extends RuntimeException {
    public QuestionMismatchException(String expectedQuestionId, String receivedQuestionId) {
        super("A pergunta atual da sessão é '" + expectedQuestionId
                + "', mas foi recebida resposta para '" + receivedQuestionId + "'");
    }
}
