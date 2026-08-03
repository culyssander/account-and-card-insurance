package com.santander.msclaimsservices.clients;

import com.santander.msclaimsservices.config.FeignConfig;
import com.santander.msclaimsservices.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MS-AUTH-SERVICES", configuration = FeignConfig.class)
public interface UserServicesClients {

    @GetMapping(path = "/v1/users/email/{email}")
    UserResponseDto findByEmail(@PathVariable String email);

    @GetMapping(path = "/v1/insureds/logged")
    UserResponseDto findByInsuredLogged();

    @GetMapping(path = "/v1/users/user-logged")
    UserResponseDto findByUserLogged();
}
