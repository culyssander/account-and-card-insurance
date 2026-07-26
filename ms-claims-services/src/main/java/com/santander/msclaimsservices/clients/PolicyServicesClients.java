package com.santander.msclaimsservices.clients;

import com.santander.msclaimsservices.dto.PolicyResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "policyServicesClients", url = "${policy.services.url}")
public interface PolicyServicesClients {

    @GetMapping("/policy-number/{policyNumber}")
    PolicyResponseDto findByPolicyNumber(@PathVariable String policyNumber);

}
