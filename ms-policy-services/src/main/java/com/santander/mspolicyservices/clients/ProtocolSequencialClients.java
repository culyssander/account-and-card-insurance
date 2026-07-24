package com.santander.mspolicyservices.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "protocolSequencialClients", url = "${protocol.services.url}")
public interface ProtocolSequencialClients {

    @PostMapping
    String generateProtocol(@RequestParam String code);

}
