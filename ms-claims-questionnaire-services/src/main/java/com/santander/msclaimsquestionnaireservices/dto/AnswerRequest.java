package com.santander.msclaimsquestionnaireservices.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
        @NotBlank String questionId,
        @NotBlank String selectedOption
) {
}
