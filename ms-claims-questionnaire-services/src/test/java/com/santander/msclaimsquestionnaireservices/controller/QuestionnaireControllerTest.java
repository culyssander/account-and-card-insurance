package com.santander.msclaimsquestionnaireservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.msclaimsquestionnaireservices.dto.AnswerRequest;
import com.santander.msclaimsquestionnaireservices.dto.AnswerResponse;
import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import com.santander.msclaimsquestionnaireservices.dto.OptionResponse;
import com.santander.msclaimsquestionnaireservices.dto.QuestionResponse;
import com.santander.msclaimsquestionnaireservices.service.QuestionnaireService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionnaireController.class)
class QuestionnaireControllerTest {

    private static final String CLAIM_ID = "claim-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionnaireService questionnaireService;

    @Test
    void start_returnsFirstQuestionFromService() throws Exception {
        QuestionResponse response = new QuestionResponse("Q001", "O cartão foi furtado ou roubado?", null, List.of());
        when(questionnaireService.start(CLAIM_ID)).thenReturn(response);

        mockMvc.perform(get("/claims/{claimId}/questionnaire/start", CLAIM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value("Q001"));

        verify(questionnaireService).start(CLAIM_ID);
    }

    @Test
    void answer_whenNonTerminal_delegatesToServiceAndReturnsNextQuestion() throws Exception {
        AnswerRequest request = new AnswerRequest("Q001", "OPT1");
        AnswerResponse response = AnswerResponse.ofNextQuestion(
                new QuestionResponse("Q002", "próxima pergunta",
                        null, List.of(new OptionResponse("OPT1", "Sim"))));

        when(questionnaireService.answer(eq(CLAIM_ID), any(AnswerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/claims/{claimId}/questionnaire/answer", CLAIM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.nextQuestion.questionId").value("Q002"));

        verify(questionnaireService).answer(eq(CLAIM_ID), any(AnswerRequest.class));
    }

    @Test
    void answer_whenSelectedOptionIsBlank_shouldReturnBadRequest() throws Exception {
        String invalidBody = "{\"questionId\":\"Q001\",\"selectedOption\":\"\"}";

        mockMvc.perform(post("/claims/{claimId}/questionnaire/answer", CLAIM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByClaimId_returnsClaimFromService() throws Exception {
        ClaimResponseDto claim = mock(ClaimResponseDto.class);
        when(questionnaireService.findQuestionnaireByClaimIdDto(CLAIM_ID)).thenReturn(claim);

        mockMvc.perform(get("/claims/{claimId}/questionnaire", CLAIM_ID))
                .andExpect(status().isOk());

        verify(questionnaireService).findQuestionnaireByClaimIdDto(CLAIM_ID);
    }
}