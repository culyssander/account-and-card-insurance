package com.santander.mspolicyservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PolicyResponseDto {
    private String policyNumber;
    private ProductResponseDto produtos;
    private String cpf;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
