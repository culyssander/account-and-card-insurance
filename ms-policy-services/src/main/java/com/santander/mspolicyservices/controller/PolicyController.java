package com.santander.mspolicyservices.controller;

import com.santander.mspolicyservices.dto.PolicyRequestDto;
import com.santander.mspolicyservices.dto.PolicyResponseDto;
import com.santander.mspolicyservices.services.PolicyServices;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/policies")
@AllArgsConstructor
public class PolicyController {

    private PolicyServices policyServices;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PolicyResponseDto createPolicy(@Validated @RequestBody PolicyRequestDto policyRequest, HttpServletRequest request, Locale locale) {
        return policyServices.createPolicy(policyRequest, request, locale);
    }

    @GetMapping("/product-number/{productNumber}")
    public PolicyResponseDto findByProductNumber(@PathVariable String productNumber, Locale locale) {
        return policyServices.findByAdminOrAnalysis(productNumber, locale);
    }

    @GetMapping("insured/product-number/{productNumber}")
    public PolicyResponseDto findByCPFAndProductNumber(@PathVariable String productNumber, Locale locale) {
        return policyServices.findByAdminOrAnalysis(productNumber, locale);
    }
}
