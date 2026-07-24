package com.santander.mspolicyservices.dto;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private BigInteger id;
    private String name;
    private String cpf;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
