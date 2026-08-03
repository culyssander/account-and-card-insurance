package com.santander.msauthservices.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InsuredResponseDto {
    private Long id;
    private String name;
    private String cpf;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
