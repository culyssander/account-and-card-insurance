package com.santander.msclaimsservices.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClaimRequestDto {
    @NotBlank
    private String policyNumber;

    @NotBlank
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private String eventDate;

    @NotBlank
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private String openingDate;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal claimedAmount;
}
