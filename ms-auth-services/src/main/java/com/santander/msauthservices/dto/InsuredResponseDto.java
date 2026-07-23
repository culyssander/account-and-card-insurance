package com.santander.msauthservices.dto;


import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class InsuredResponseDto {
    private BigInteger id;
    private String name;
    private String cpf;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
