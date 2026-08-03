package com.santander.msclaimsservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.msclaimsservices.dto.ClaimRequestStatusDto;
import com.santander.msclaimsservices.dto.ClaimResponseDto;
import com.santander.msclaimsservices.services.ClaimServices;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClaimServices claimServices;

    private ClaimResponseDto buildResponse(String claimNumber) {
        return ClaimResponseDto.builder()
                .claimNumber(claimNumber)
                .status("OPEN")
                .description("Furto de veiculo")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /v1/claims")
    class NewClaim {

        @Test
        @DisplayName("deve retornar 201 quando enviado com arquivos e o payload 'claim'")
        void shouldReturn201WithFilesAndClaimPayload() throws Exception {
            ClaimResponseDto response = buildResponse("SIN-001");
            String claimJson = "{\"policyNumber\":\"PROTOCOL-001\",\"description\":\"Furto de veiculo\"}";

            MockMultipartFile file = new MockMultipartFile(
                    "files", "documento.jpg", "image/jpeg", "conteudo".getBytes());

            when(claimServices.newClaimDto(anyList(), eq(claimJson), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(multipart("/v1/claims")
                            .file(file)
                            .param("claim", claimJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.claimNumber").value("SIN-001"));

            verify(claimServices).newClaimDto(anyList(), eq(claimJson), any(Locale.class));
        }

        @Test
        @DisplayName("deve retornar 201 quando enviado sem arquivos (files é opcional)")
        void shouldReturn201WithoutFiles() throws Exception {
            ClaimResponseDto response = buildResponse("SIN-002");
            String claimJson = "{\"policyNumber\":\"PROTOCOL-002\",\"description\":\"Colisao\"}";

            when(claimServices.newClaimDto(isNull(), eq(claimJson), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(multipart("/v1/claims")
                            .param("claim", claimJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.claimNumber").value("SIN-002"));
        }

        @Test
        @DisplayName("deve retornar 400 quando o parâmetro 'claim' obrigatório está ausente")
        void shouldReturn400WhenClaimParamMissing() throws Exception {
            mockMvc.perform(multipart("/v1/claims"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /v1/claims/{claimId}")
    class FindByClaimId {

        @Test
        @DisplayName("deve retornar 200 com os dados do sinistro")
        void shouldReturn200WithClaimData() throws Exception {
            ClaimResponseDto response = buildResponse("SIN-001");

            when(claimServices.findByClaimNumberDto(eq("SIN-001"), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/claims/{claimId}", "SIN-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.claimNumber").value("SIN-001"));
        }

    }

    @Nested
    @DisplayName("PUT /v1/claims/{claimId}/status")
    class UpdateStatus {

        @Test
        @DisplayName("deve retornar 200 ao atualizar status via updateStatusDocOrReview")
        void shouldReturn200WhenUpdatingStatus() throws Exception {
            ClaimRequestStatusDto requestDto = ClaimRequestStatusDto.builder()
                    .status("DOCUMENTATION_PENDING")
                    .build();
            ClaimResponseDto response = ClaimResponseDto.builder()
                    .claimNumber("SIN-001")
                    .status("DOCUMENTATION_PENDING")
                    .build();

            when(claimServices.updateStatusDocOrReview(eq("SIN-001"), any(ClaimRequestStatusDto.class), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/v1/claims/{claimId}/status", "SIN-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DOCUMENTATION_PENDING"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/claims/{claimId}/status/analysis")
    class UpdateStatusByAnalysis {

        @Test
        @DisplayName("deve retornar 200 ao atualizar status via updateStatusApprovedOrDenied")
        void shouldReturn200WhenUpdatingStatusByAnalysis() throws Exception {
            ClaimRequestStatusDto requestDto = ClaimRequestStatusDto.builder()
                    .status("APPROVED")
                    .build();
            ClaimResponseDto response = ClaimResponseDto.builder()
                    .claimNumber("SIN-001")
                    .status("APPROVED")
                    .build();

            when(claimServices.updateStatusApprovedOrDenied(eq("SIN-001"), any(ClaimRequestStatusDto.class), any(Locale.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/v1/claims/{claimId}/status/analysis", "SIN-001")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }
    }
}