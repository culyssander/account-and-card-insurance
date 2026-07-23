package com.santander.msauthservices.controller;

import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.dto.UserResponseDto;
import com.santander.msauthservices.services.UserServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
//@Tag(name = "User", description = "Endpoints for managing users")
public class UserController {

    private UserServices userServices;

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponseDto newUser(@Validated @RequestBody UserRequestDto dto, Locale locale) {
        return userServices.newUser(dto, locale);
    }

    @PreAuthorize("hasAnyRole('ANALISTA', 'ADMIN')")
    @GetMapping("/email/{email}")
    public UserResponseDto findUserByEmail(@PathVariable String email, Locale locale) {
        return userServices.findByEmailDto(email, locale);
    }
}
