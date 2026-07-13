package com.ines.skillmatch_offres_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Feign client for the Candidature microservice. Uses Consul service discovery and a fallback for resilience.
@FeignClient(name = "skillmatch-candidature-service", fallback = CandidatureClientFallback.class)
public interface CandidatureClient {

    // Counts the number of applications (candidatures) for a specific job offer.
    @GetMapping("/api/candidatures/count")
    Long CountByOffreId(@RequestParam("offreId") Long offreId);
}