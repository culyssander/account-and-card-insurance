package com.santander.mspolicyservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CoverageDto {
    private String code;
    private String name;
    private String description;
    private boolean mandatory;
}
