package com.santander.msclaimsquestionnaireservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsClaimsQuestionnaireServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsClaimsQuestionnaireServicesApplication.class, args);
	}

}
