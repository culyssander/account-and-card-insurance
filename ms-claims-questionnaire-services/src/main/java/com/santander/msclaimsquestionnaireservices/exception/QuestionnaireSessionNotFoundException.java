package com.santander.msclaimsquestionnaireservices.exception;

public class QuestionnaireSessionNotFoundException extends RuntimeException {
    public QuestionnaireSessionNotFoundException(String claimId, String questionId) {
        super("Sessão de questionário não encontrada para o sinistro " + claimId
                + " ou questionId "+ questionId + ". Chame GET /claims/questionnaire/start primeiro.");
    }
}
