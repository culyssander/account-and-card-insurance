package com.santander.msclaimsservices.controller;

import com.santander.msclaimsservices.dto.ClaimRequestStatusDto;
import com.santander.msclaimsservices.dto.ClaimResponseDto;
import com.santander.msclaimsservices.services.ClaimServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/v1/claims")
@AllArgsConstructor
public class ClaimController {

    private ClaimServices claimServices;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponseDto newClaim (@RequestParam(value = "files", required = false) List<MultipartFile> files,
                                      @RequestParam(name = "claim") String claim, Locale locale) {
        return claimServices.newClaimDto(files, claim, locale);
    }

    @GetMapping("/{claimId}")
    public ClaimResponseDto findByClaimId(@PathVariable String claimId, Locale locale) {
        return claimServices.findByClaimNumberDto(claimId, locale);
    }

    @PutMapping("/{claimId}/status")
    public ClaimResponseDto updateStatus(@Validated  @PathVariable String claimId, @RequestBody ClaimRequestStatusDto requestStatus, Locale locale) {
        return claimServices.updateStatusDocOrReview(claimId, requestStatus, locale);
    }

    @PutMapping("/{claimId}/status/analysis")
    public ClaimResponseDto updateStatusByAnalysis(@Validated  @PathVariable String claimId, @RequestBody ClaimRequestStatusDto requestStatus, Locale locale) {
        return claimServices.updateStatusApprovedOrDenied(claimId, requestStatus,locale);
    }
}
