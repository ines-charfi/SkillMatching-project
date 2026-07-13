package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

// Feign client for the Candidature microservice. Uses Consul service discovery and a fallback for resilience.
@FeignClient(name = "skillmatch-candidature-service", fallback = CandidatureClientFallback.class)
public interface CandidatureClient {

    // Counts the total number of job applications (candidatures) across the platform.
    @GetMapping("/api/candidatures/count/all")
    Long countAllCandidatures();
}