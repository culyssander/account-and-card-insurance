package com.santander.msclaimsquestionnaireservices.clients;

import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-CLAIMS-SERVICES")
public interface ClaimServicesClients {

    @GetMapping("/v1/claims/{claimId}")
    ClaimResponseDto findClaimById(@PathVariable String claimId);
}
