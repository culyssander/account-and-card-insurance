package com.santander.msclaimsquestionnaireservices.controller;

import com.santander.msclaimsquestionnaireservices.dto.AnswerRequest;
import com.santander.msclaimsquestionnaireservices.dto.AnswerResponse;
import com.santander.msclaimsquestionnaireservices.dto.QuestionResponse;
import com.santander.msclaimsquestionnaireservices.service.QuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/claims/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    // GET /claims/questionnaire/start?claimId=123456
    // claimId como query param: precisamos saber a qual sinistro a sessão
    // pertence desde a primeira pergunta, já que o POST /answer não
    // reenvia contexto nenhum além do questionId e da opção escolhida.
    @GetMapping("/start")
    public QuestionResponse start(@RequestParam String claimId) {
        return questionnaireService.start(claimId);
    }

    @PostMapping("/answer")
    public AnswerResponse answer(@Valid @RequestBody AnswerRequest request) {
        return questionnaireService.answer(request);
    }
}
