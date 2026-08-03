package com.santander.msauthservices.controller;

import com.santander.msauthservices.config.SecurityConfig;
import com.santander.msauthservices.model.Insured;
import com.santander.msauthservices.services.InsuredServices;
import com.santander.msauthservices.services.UserServices;
import com.santander.msauthservices.util.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = InsuredController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
class InsuredControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsuredServices insuredServices;

    @MockitoBean
    private UserServices userServices;

    @MockitoBean
    private JWTUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ANALISTA")
    void findUserByEmail_deveRetornar200_quandoAnalistaAutenticado() throws Exception {
        String cpf = "12345678900";
        Insured insured = Insured.builder().cpf(cpf).name("Fulano").build();

        when(insuredServices.findByCPF(eq(cpf), any(Locale.class))).thenReturn(insured);

        mockMvc.perform(get("/v1/insureds/cpf/{cpf}", cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(cpf));
    }

    @Test
    @WithMockUser(roles = "INSURED")
    void findUserByEmail_deveRetornar403_quandoRoleNaoAutorizada() throws Exception {
        mockMvc.perform(get("/v1/insureds/cpf/{cpf}", "12345678900"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSURED")
    void findUserLogged_deveRetornar200_quandoSeguradoAutenticado() throws Exception {
        String email = "segurado@teste.com";
        Insured insured = Insured.builder().email(email).name("Segurado").build();

        when(userServices.userLogged(any(Locale.class))).thenReturn(email);
        when(insuredServices.findByInsureLogged(eq(email), any(Locale.class))).thenReturn(insured);

        mockMvc.perform(get("/v1/insureds/logged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findUserLogged_deveRetornar403_quandoUsuarioNaoForInsured() throws Exception {
        mockMvc.perform(get("/v1/insureds/logged"))
                .andExpect(status().isForbidden());
    }
}