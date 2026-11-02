package com.evidencija.evidencija_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EvidencijaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvidencijaBackendApplication.class, args);
	}

}
