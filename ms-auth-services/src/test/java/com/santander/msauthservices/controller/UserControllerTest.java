package com.santander.msauthservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.msauthservices.config.SecurityConfig;
import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.dto.UserResponseDto;
import com.santander.msauthservices.model.Role;
import com.santander.msauthservices.services.UserServices;
import com.santander.msauthservices.util.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServices userServices;

    @MockitoBean
    private JWTUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void newUser_deveRetornar201_quandoAdminAutenticado() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Fulano");
        request.setEmail("fulano@teste.com");
        request.setPassword("123456");
        request.setRole(Role.ADMIN);

        UserResponseDto response = UserResponseDto.builder()
                .name("Fulano")
                .email("fulano@teste.com")
                .build();

        when(userServices.newUser(any(UserRequestDto.class), any(Locale.class))).thenReturn(response);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("fulano@teste.com"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    void newUser_deveRetornar403_quandoUsuarioNaoForAdmin() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setName("Fulano");
        request.setEmail("fulano@teste.com");
        request.setPassword("123456");
        request.setRole(Role.ADMIN);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    void findUserByEmail_deveRetornar200_quandoAnalistaAutenticado() throws Exception {
        String email = "fulano@teste.com";
        UserResponseDto response = UserResponseDto.builder()
                .name("Fulano")
                .email(email)
                .build();

        when(userServices.findByEmailDto(eq(email), any(Locale.class))).thenReturn(response);

        mockMvc.perform(get("/v1/users/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithMockUser(roles = "INSURED")
    void findUserByEmail_deveRetornar403_quandoRoleNaoAutorizada() throws Exception {
        mockMvc.perform(get("/v1/users/email/{email}", "fulano@teste.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findUserByEmailLogged_deveRetornarUsuarioLogado() throws Exception {
        String email = "logado@teste.com";
        UserResponseDto response = UserResponseDto.builder()
                .name("Logado")
                .email(email)
                .build();

        when(userServices.userLogged(any(Locale.class))).thenReturn(email);
        when(userServices.findByEmailDto(eq(email), any(Locale.class))).thenReturn(response);

        mockMvc.perform(get("/v1/users/user-logged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }
}