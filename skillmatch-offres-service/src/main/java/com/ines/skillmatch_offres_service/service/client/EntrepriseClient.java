package com.ines.skillmatch_offres_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

// Feign client for the Entreprise microservice. Uses Consul service discovery and a fallback for resilience.
@FeignClient(name = "skillmatch-entreprise-service", fallback = EntrepriseClientFallback.class)
public interface EntrepriseClient {

    // Fetches a company profile by the associated user ID.
    @GetMapping("/api/entreprises/user/{userId}")
    Map<String, Object> getEntrepriseByUserId(@PathVariable("userId") Long userId);

    // Fetches a company profile by its internal ID.
    @GetMapping("/api/entreprises/{id}")
    Map<String, Object> getById(@PathVariable("id") Long id);
}