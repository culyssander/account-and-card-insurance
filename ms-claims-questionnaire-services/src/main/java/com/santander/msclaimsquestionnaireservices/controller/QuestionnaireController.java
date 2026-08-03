package com.santander.msclaimsquestionnaireservices.controller;

import com.santander.msclaimsquestionnaireservices.dto.AnswerRequest;
import com.santander.msclaimsquestionnaireservices.dto.AnswerResponse;
import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import com.santander.msclaimsquestionnaireservices.dto.QuestionResponse;
import com.santander.msclaimsquestionnaireservices.service.QuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/claims/{claimId}/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @GetMapping("/start")
    public QuestionResponse start(@PathVariable String claimId) {
        return questionnaireService.start(claimId);
    }

    @PostMapping("/answer")
    public AnswerResponse answer(@Valid @PathVariable String claimId, @RequestBody AnswerRequest request) {
        return questionnaireService.answer(claimId, request);
    }

    @GetMapping
    public ClaimResponseDto findByClaimId(@PathVariable String claimId) {
        return questionnaireService.findQuestionnaireByClaimIdDto(claimId);
    }
}
