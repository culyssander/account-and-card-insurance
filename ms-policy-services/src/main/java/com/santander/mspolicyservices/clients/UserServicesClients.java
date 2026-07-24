package com.santander.mspolicyservices.clients;

import com.santander.mspolicyservices.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userServicesClients", url = "${auth.services.url}")
public interface UserServicesClients {

    @GetMapping(path = "/users/email/{email}")
    UserResponseDto findByEmail(@PathVariable String email);

    @GetMapping(path = "/insureds/cpf/{cpf}")
    UserResponseDto findByCpf(@PathVariable String cpf);
}
