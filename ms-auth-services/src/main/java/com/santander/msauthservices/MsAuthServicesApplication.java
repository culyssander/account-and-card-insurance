package com.santander.msauthservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MsAuthServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAuthServicesApplication.class, args);
	}

}
