package com.santander.mspolicyservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsPolicyServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsPolicyServicesApplication.class, args);
	}

}
