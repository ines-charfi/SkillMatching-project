package com.ines.skillmatch_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients   //--- TRÈS IMPORTANT : Permet d'utiliser les interfaces CandidatClient/EntrepriseClient

public class SkillmatchAuthServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(SkillmatchAuthServiceApplication.class, args);
	}

}
