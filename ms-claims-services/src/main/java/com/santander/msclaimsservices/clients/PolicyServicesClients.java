package com.santander.msclaimsservices.clients;

import com.santander.msclaimsservices.config.FeignConfig;
import com.santander.msclaimsservices.dto.PolicyResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-POLICY-SERVICES", configuration = FeignConfig.class)
public interface PolicyServicesClients {

    @GetMapping("/v1/policies/insured/policy-number/{policyNumber}")
    PolicyResponseDto findByPolicyNumber(@PathVariable String policyNumber);

}
