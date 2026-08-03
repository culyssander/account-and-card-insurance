package com.santander.protocoloservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MsProtocolServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsProtocolServicesApplication.class, args);
	}

}
