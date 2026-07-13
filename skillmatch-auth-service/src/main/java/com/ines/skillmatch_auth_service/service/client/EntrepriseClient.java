package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

// Feign client for the Entreprise microservice. Uses Consul service discovery (name) and a fallback for resilience.
@FeignClient(name = "skillmatch-entreprise-service", fallback = EntrepriseClientFallback.class)
public interface EntrepriseClient {

    // Initializes a company profile after user registration (creates base record with the company name).
    @PostMapping("/api/entreprises/init")
    void initEntreprise(@RequestParam("userId") Long userId,
                        @RequestParam("nom") String nom);

    // Fetches the complete company profile by the associated user ID.
    @GetMapping("/api/entreprises/user/{userId}")
    Map<String, Object> getEntrepriseByUserId(@PathVariable("userId") Long userId);
}