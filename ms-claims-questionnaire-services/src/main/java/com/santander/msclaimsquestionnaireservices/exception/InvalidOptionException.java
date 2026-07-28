package com.santander.msclaimsquestionnaireservices.exception;

public class InvalidOptionException extends RuntimeException {
    public InvalidOptionException(String questionId, String optionId) {
        super("Opção '" + optionId + "' não é válida para a pergunta " + questionId);
    }
}
