package com.santander.msclaimsquestionnaireservices.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ClaimResponseDto {
    private BigInteger id;
    private String claimNumber;
    private String status;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime openingDate;
    private BigDecimal claimedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AnsweredQuestionDto> allAnsweredQuestion;
}
