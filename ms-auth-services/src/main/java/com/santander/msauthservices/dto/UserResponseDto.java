package com.santander.msauthservices.dto;

import com.santander.msauthservices.model.Role;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private BigInteger id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
