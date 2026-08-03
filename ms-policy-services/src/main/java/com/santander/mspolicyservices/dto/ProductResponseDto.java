package com.santander.mspolicyservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductResponseDto {
    private BigInteger id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private List<CoverageDto> coverages;
}
