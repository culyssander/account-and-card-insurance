package com.santander.msauthservices.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
