package com.santander.protocoloservice.controller;

import com.santander.protocoloservice.services.ProtocolSequencialServicos;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/protocols")
@AllArgsConstructor
public class ProtocolController {

    private ProtocolSequencialServicos protocolSequencialServicos;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String createProtocol(String code) {
        return protocolSequencialServicos.generateProtocol(code);
    }
}
