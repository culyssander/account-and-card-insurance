package com.santander.msauthservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.msauthservices.config.SecurityConfig;
import com.santander.msauthservices.dto.LoginRequestDto;
import com.santander.msauthservices.exception.BadRequestException;
import com.santander.msauthservices.util.JWTUtil;
import com.santander.msauthservices.services.UserServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@EnableMethodSecurity
@WebMvcTest(controllers = LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private MessageSource messageSource;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private UserServices userServices;

    @Test
    void sign_deveRetornarTokenComStatus200_quandoCredenciaisValidas() throws Exception {
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("fulano@teste.com");
        login.setPassword("senha123");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());
        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationToken);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("login válido");
        when(jwtUtil.generateToken(login.getEmail())).thenReturn("token-fake-123");

        mockMvc.perform(post("/v1/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("token-fake-123")));
    }

    @Test
    void sign_deveLancarBadRequestException_quandoCredenciaisInvalidas() throws Exception {
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("fulano@teste.com");
        login.setPassword("senha-errada");

        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Credenciais inválidas");

        Throwable resolved;
        try {
            MvcResult result = mockMvc.perform(post("/v1/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andReturn();
            resolved = result.getResolvedException();
        } catch (Exception thrown) {
            resolved = thrown.getCause() != null ? thrown.getCause() : thrown;
        }

        assertThat(resolved).isInstanceOf(BadRequestException.class);
        assertThat(resolved.getMessage()).isEqualTo("Credenciais inválidas");
    }
}