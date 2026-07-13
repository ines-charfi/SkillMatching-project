package com.ines.skillmatch_offres_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Main entry point for the Offer microservice.
// Manages job offers, cross-service enrichment (company info, application counts), and notifications.
@SpringBootApplication
// Registers this service with Consul (service registry) so it can be discovered by other services.
@EnableDiscoveryClient
// Enables Feign client proxies – required for using @FeignClient interfaces (EntrepriseClient, CandidatureClient, NotificationClient).
@EnableFeignClients
public class SkillmatchOffresServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillmatchOffresServiceApplication.class, args);
	}
}