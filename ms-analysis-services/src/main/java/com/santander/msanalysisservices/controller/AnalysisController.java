package com.santander.msanalysisservices.controller;

import com.santander.msanalysisservices.dto.AnalysisRequestDto;
import com.santander.msanalysisservices.dto.AnalysisResponseDto;
import com.santander.msanalysisservices.model.Analysis;
import com.santander.msanalysisservices.services.AnalysisServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/analysis")
@AllArgsConstructor
public class AnalysisController {

    private AnalysisServices analysisServices;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AnalysisResponseDto save(@Validated @RequestBody AnalysisRequestDto request) {
        return analysisServices.save(request);
    }

    @GetMapping
    public List<AnalysisResponseDto> findAll() {
        return analysisServices.findAll();
    }

    @GetMapping("/claim/{claimId}")
    public AnalysisResponseDto findByClaimId(@PathVariable String claimId) {
        return analysisServices.findByClaimIdDto(claimId);
    }

}
