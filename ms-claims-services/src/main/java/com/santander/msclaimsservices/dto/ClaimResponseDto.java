package com.santander.msclaimsservices.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaimResponseDto {
    private BigInteger id;
    private PolicyResponseDto policy;
    private String claimNumber;
    private String status;
    private String description;
    private LocalDateTime eventDate;
    private LocalDateTime openingDate;
    private BigDecimal claimedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String usuario;
}
