package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "skillmatch-candidature-service", fallback = CandidatureClientFallback.class)
public interface CandidatureClient {
    @GetMapping("/api/candidatures/count/all") // Endpoint à créer dans CandidatureController
    Long countAllCandidatures();
}
