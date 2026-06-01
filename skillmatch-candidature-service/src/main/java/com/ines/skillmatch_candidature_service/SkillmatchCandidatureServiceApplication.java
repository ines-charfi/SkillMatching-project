package com.ines.skillmatch_candidature_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SkillmatchCandidatureServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillmatchCandidatureServiceApplication.class, args);
	}

}
