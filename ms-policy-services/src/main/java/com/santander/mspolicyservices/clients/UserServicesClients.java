package com.santander.mspolicyservices.clients;

import com.santander.mspolicyservices.config.FeignConfig;
import com.santander.mspolicyservices.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "userServicesClients", url = "${auth.services.url}")
@FeignClient(name = "MS-AUTH-SERVICES", configuration = FeignConfig.class)
public interface UserServicesClients {

    @GetMapping(path = "/users/email/{email}")
    UserResponseDto findByEmail(@PathVariable String email);

    @GetMapping(path = "/v1/insureds/logged")
    UserResponseDto findByInsuredLogged();

    @GetMapping(path = "/users/user-logged")
    UserResponseDto findByUserLogged();
}
