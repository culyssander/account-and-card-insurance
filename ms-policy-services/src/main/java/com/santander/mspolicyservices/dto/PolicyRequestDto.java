package com.santander.mspolicyservices.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PolicyRequestDto {
    @NotBlank
    private String productCode;
}
