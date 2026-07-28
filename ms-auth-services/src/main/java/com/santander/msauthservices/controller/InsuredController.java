package com.santander.msauthservices.controller;

import com.santander.msauthservices.model.Insured;
import com.santander.msauthservices.services.InsuredServices;
import com.santander.msauthservices.services.UserServices;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/v1/insureds")
@AllArgsConstructor
//@Tag(name = "Insured", description = "Endpoints for managing Insureds")
public class InsuredController {

    private InsuredServices insuredServices;
    private UserServices userServices;

    @PreAuthorize("hasAnyRole('ANALISTA', 'ADMIN')")
    @GetMapping("/cpf/{cpf}")
    public Insured findUserByEmail(@PathVariable String cpf, Locale locale) {
        return insuredServices.findByCPF(cpf, locale);
    }

    @PreAuthorize("hasRole('INSURED')")
    @GetMapping("/logged")
    public Insured findUserByCPF(Locale locale) {
        String email = userServices.userLogged(locale);
        return insuredServices.findByInsureLogged(email, locale);
    }

}
