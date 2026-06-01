package com.ines.frontend_skillmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FrontendSkillmatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontendSkillmatchApplication.class, args);
	}

}
