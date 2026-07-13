package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

// Feign client for the Candidate microservice. Uses Consul service discovery (name) and a fallback for resilience.
@FeignClient(name = "skillmatch-candidat-service", fallback = CandidatClientFallback.class)
public interface CandidatClient {

    // Initializes a candidate profile after user registration (creates base record with name and first name).
    @PostMapping("/api/candidats/init")
    void initCandidat(@RequestParam("userId") Long userId,
                      @RequestParam("nom") String nom,
                      @RequestParam("prenom") String prenom);

    // Fetches the complete candidate profile by the associated user ID.
    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getCandidatByUserId(@PathVariable("userId") Long userId);
}