package com.ines.skillmatch_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// Main entry point for the authentication microservice.
// Combines Spring Boot auto-configuration, service discovery, and Feign clients.
@SpringBootApplication
// Registers this service with Consul (service registry) so it can be discovered by other services.
@EnableDiscoveryClient
// Enables Feign client proxies – required for using @FeignClient interfaces (CandidatClient, EntrepriseClient, etc.).
@EnableFeignClients
public class SkillmatchAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillmatchAuthServiceApplication.class, args);
	}
}