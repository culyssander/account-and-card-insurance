package com.santander.msanalysisservices.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.santander.msanalysisservices.model.AnalysisResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AnalysisRequestDto {
    @NotBlank
    private String claimId;

    @NotNull
    private AnalysisResult result;

    private String reasonForDenial;
    private BigDecimal compensationAmount = BigDecimal.ZERO;

    @NotBlank
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private String analysisDate;
}
