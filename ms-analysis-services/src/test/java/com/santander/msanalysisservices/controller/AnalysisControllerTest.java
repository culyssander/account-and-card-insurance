package com.santander.msanalysisservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.msanalysisservices.dto.AnalysisRequestDto;
import com.santander.msanalysisservices.dto.AnalysisResponseDto;
import com.santander.msanalysisservices.exception.NotFoundException;
import com.santander.msanalysisservices.model.AnalysisResult;
import com.santander.msanalysisservices.services.AnalysisServices;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

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

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnalysisServices analysisServices;

    private AnalysisRequestDto buildValidRequest() {
        return AnalysisRequestDto.builder()
                .claimId("SIN-001")
                .result(AnalysisResult.APPROVED)
                .compensationAmount(new BigDecimal("1500.00"))
                .analysisDate("01/07/2026 10:30")
                .build();
    }

    private AnalysisResponseDto buildResponse() {
        return AnalysisResponseDto.builder()
                .claimId("SIN-001")
                .result(AnalysisResult.APPROVED)
                .build();
    }

    @Nested
    @DisplayName("POST /v1/analysis")
    class Save {

        @Test
        @DisplayName("deve retornar 201 quando o payload é válido")
        void shouldReturn201WhenPayloadIsValid() throws Exception {
            AnalysisRequestDto request = buildValidRequest();
            AnalysisResponseDto response = buildResponse();

            when(analysisServices.save(any(AnalysisRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.claimId").value("SIN-001"));

            verify(analysisServices).save(any(AnalysisRequestDto.class));
        }

        @Test
        @DisplayName("deve retornar 400 quando o corpo da requisição é inválido")
        void shouldReturn400WhenPayloadIsInvalid() throws Exception {
            mockMvc.perform(post("/v1/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /v1/analysis")
    class FindAll {

        @Test
        @DisplayName("deve retornar 200 com a lista de análises")
        void shouldReturn200WithAnalysisList() throws Exception {
            when(analysisServices.findAll()).thenReturn(List.of(buildResponse()));

            mockMvc.perform(get("/v1/analysis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].claimId").value("SIN-001"));
        }

        @Test
        @DisplayName("deve retornar 200 com lista vazia quando não há análises")
        void shouldReturn200WithEmptyList() throws Exception {
            when(analysisServices.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/v1/analysis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /v1/analysis/claim/{claimId}")
    class FindByClaimId {

        @Test
        @DisplayName("deve retornar 200 com a análise do sinistro")
        void shouldReturn200WithAnalysis() throws Exception {
            when(analysisServices.findByClaimIdDto(eq("SIN-001"))).thenReturn(buildResponse());

            mockMvc.perform(get("/v1/analysis/claim/{claimId}", "SIN-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.claimId").value("SIN-001"));
        }

    }
}