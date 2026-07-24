package com.santander.mspolicyservices.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class ProductResponseDto {
    private BigInteger id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private List<CoverageDto> coverages;
}
