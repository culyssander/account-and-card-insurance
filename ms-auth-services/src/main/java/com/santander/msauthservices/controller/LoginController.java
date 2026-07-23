package com.santander.msauthservices.controller;

import com.santander.msauthservices.constants.UserConstants;
import com.santander.msauthservices.dto.LoginRequestDto;
import com.santander.msauthservices.exception.BadRequestException;
import com.santander.msauthservices.util.JWTUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/login")
@AllArgsConstructor
public class LoginController {

    private final AuthenticationProvider authenticationProvider;
    private final MessageSource messageSource;
    private final JWTUtil jwtUtil;

    @PostMapping
    public ResponseEntity<String> sign(@Validated @RequestBody LoginRequestDto login, Locale locale) {
        try {
            authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        } catch (Exception ex) {
            throw new BadRequestException(messageSource.getMessage(UserConstants.USER_LOGIN_INVALID, new Object[] {}, locale));
        }

        String token = String.format("{\"token\": \"%s\"}", jwtUtil.generateToken(login.getEmail()));
        return ResponseEntity.ok(token);
    }
}
