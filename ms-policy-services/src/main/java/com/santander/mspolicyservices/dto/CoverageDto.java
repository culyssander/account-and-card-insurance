package com.santander.mspolicyservices.dto;

import lombok.Data;

@Data
public class CoverageDto {
    private String code;
    private String name;
    private String description;
    private boolean mandatory;
}
