package com.santander.mspolicyservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.mspolicyservices.dto.PolicyRequestDto;
import com.santander.mspolicyservices.dto.PolicyResponseDto;
import com.santander.mspolicyservices.exception.AccessDeniedException;
import com.santander.mspolicyservices.exception.NotFoundException;
import com.santander.mspolicyservices.services.PolicyServices;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PolicyServices policyServices;

    private PolicyResponseDto buildResponse(String policyNumber) {
        return PolicyResponseDto.builder()
                .policyNumber(policyNumber)
                .cpf("12345678900")
                .status("DRAFT")
                .startDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /v1/policies")
    class CreatePolicy {

        @Test
        @DisplayName("deve retornar 201 e a apólice criada quando payload é válido")
        void shouldReturn201WhenPayloadIsValid() throws Exception {
            PolicyRequestDto request = PolicyRequestDto.builder()
                    .productCode("PROD-001")
                    .build();

            PolicyResponseDto response = buildResponse("PROTOCOL-001");

            when(policyServices.createPolicy(any(PolicyRequestDto.class), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/policies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.policyNumber").value("PROTOCOL-001"))
                    .andExpect(jsonPath("$.cpf").value("12345678900"));

            verify(policyServices).createPolicy(any(PolicyRequestDto.class), any(Locale.class));
        }

        @Test
        @DisplayName("deve retornar 400 quando o corpo da requisição é inválido")
        void shouldReturn400WhenPayloadIsInvalid() throws Exception {
            mockMvc.perform(post("/v1/policies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /v1/policies/policy-number/{policyNumber}")
    class FindByProductNumber {

        @Test
        @DisplayName("deve retornar 200 com os dados da apólice")
        void shouldReturn200WithPolicyData() throws Exception {
            PolicyResponseDto response = buildResponse("PROTOCOL-001");

            when(policyServices.findByAdminOrAnalysis(eq("PROTOCOL-001"), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/policies/policy-number/{policyNumber}", "PROTOCOL-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.policyNumber").value("PROTOCOL-001"));
        }

        @Test
        @DisplayName("deve retornar 404 com corpo padronizado quando a apólice não é encontrada")
        void shouldReturn404WhenPolicyNotFound() throws Exception {
            when(policyServices.findByAdminOrAnalysis(eq("INEXISTENTE"), any(Locale.class)))
                    .thenThrow(new NotFoundException("Apólice não encontrada"));

            mockMvc.perform(get("/v1/policies/policy-number/{policyNumber}", "INEXISTENTE"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Apólice não encontrada"));
        }
    }

    @Nested
    @DisplayName("GET /v1/policies/insured/policy-number/{policyNumber}")
    class FindByCpfAndProductNumber {

        @Test
        @DisplayName("deve retornar 200 com os dados da apólice do segurado")
        void shouldReturn200WithInsuredPolicyData() throws Exception {
            PolicyResponseDto response = buildResponse("PROTOCOL-001");

            when(policyServices.findByInsured(eq("PROTOCOL-001"), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/policies/insured/policy-number/{policyNumber}", "PROTOCOL-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.policyNumber").value("PROTOCOL-001"));
        }

        @Test
        @DisplayName("deve propagar erro quando usuário logado não tem acesso")
        void shouldPropagateErrorWhenAccessDenied() {
            when(policyServices.findByInsured(eq("PROTOCOL-001"), any(Locale.class)))
                    .thenThrow(new AccessDeniedException("Acesso negado"));

            ServletException ex = assertThrows(ServletException.class, () ->
                    mockMvc.perform(get("/v1/policies/insured/policy-number/{policyNumber}", "PROTOCOL-001")));

            assertThat(ex.getCause()).isInstanceOf(AccessDeniedException.class);
            assertThat(ex.getCause().getMessage()).isEqualTo("Acesso negado");
        }
    }
}