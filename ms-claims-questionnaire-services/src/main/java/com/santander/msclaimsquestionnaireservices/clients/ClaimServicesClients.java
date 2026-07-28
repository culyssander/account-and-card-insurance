package com.santander.msclaimsquestionnaireservices.clients;

import com.santander.msclaimsquestionnaireservices.dto.ClaimResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "claimServicesClients", url = "${claim.services.url}")
public interface ClaimServicesClients {

    @GetMapping("/{claimId}")
    ClaimResponseDto findClaimById(@PathVariable String claimId);
}
