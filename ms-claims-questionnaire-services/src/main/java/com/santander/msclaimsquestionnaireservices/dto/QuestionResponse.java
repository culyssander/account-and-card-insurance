package com.santander.msclaimsquestionnaireservices.dto;


import com.santander.msclaimsquestionnaireservices.model.QuestionType;

import java.util.List;

public record QuestionResponse(
        String questionId,
        String question,
        QuestionType type,
        List<OptionResponse> options
) {
}
