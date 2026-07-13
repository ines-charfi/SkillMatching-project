package com.ines.frontend_skillmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

// Enables Spring Boot auto-configuration, component scanning, and configuration properties.
@SpringBootApplication
// Registers this application with the service registry (Consul) for service discovery.
@EnableDiscoveryClient
// Enables Feign clients for declarative HTTP calls to other microservices.
@EnableFeignClients
public class FrontendSkillmatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontendSkillmatchApplication.class, args);
	}

}