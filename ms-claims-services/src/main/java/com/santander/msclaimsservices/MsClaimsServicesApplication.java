package com.santander.msclaimsservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsClaimsServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsClaimsServicesApplication.class, args);
	}

}
