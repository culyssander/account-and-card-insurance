package com.santander.msanalysisservices.clients;

import com.santander.msanalysisservices.config.FeignConfig;
import com.santander.msanalysisservices.dto.UserResponseDto;
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
