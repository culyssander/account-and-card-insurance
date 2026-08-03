package com.santander.msanalysisservices.clients;

import com.santander.msanalysisservices.config.FeignConfig;
import com.santander.msanalysisservices.dto.ClaimRequestStatusDto;
import com.santander.msanalysisservices.dto.ClaimResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MS-CLAIMS-SERVICES", configuration = FeignConfig.class)
public interface ClaimServicesClients {

    @GetMapping("/v1/claims/{claimId}")
    ClaimResponseDto findByClaimId(@PathVariable String claimId);

    @PutMapping("/v1/claims/{claimId}/status/analysis")
    ClaimResponseDto updateStatus(@Validated @PathVariable String claimId, @RequestBody ClaimRequestStatusDto requestStatus);
}
