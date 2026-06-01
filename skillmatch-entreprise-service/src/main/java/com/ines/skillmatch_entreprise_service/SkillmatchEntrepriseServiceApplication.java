package com.ines.skillmatch_entreprise_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Indispensable pour la communication microservices
public class SkillmatchEntrepriseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillmatchEntrepriseServiceApplication.class, args);
	}

}
