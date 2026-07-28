package com.santander.msclaimsquestionnaireservices.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String questionId) {
        super("Pergunta não encontrada: " + questionId);
    }
}
