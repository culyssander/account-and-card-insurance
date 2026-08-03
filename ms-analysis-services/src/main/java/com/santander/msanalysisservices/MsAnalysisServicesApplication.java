package com.santander.msanalysisservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsAnalysisServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAnalysisServicesApplication.class, args);
	}

}
