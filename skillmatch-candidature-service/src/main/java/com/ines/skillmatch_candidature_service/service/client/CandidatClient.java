package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Declarative REST client for inter-service communication with the Candidate microservice.
 * This interface allows the Candidature service to fetch candidate profile details.
 * It is integrated with a fallback mechanism for fault tolerance and resilience.
 */
@FeignClient(name = "skillmatch-candidat-service", fallback = CandidatClientFallback.class)
public interface CandidatClient {

    /**
     * Retrieves a candidate's profile data from the remote service using the user ID.
     *
     * @param userId The unique identifier of the user linked to the candidate profile
     * @return A Map containing candidate profile information (e.g., skills, education)
     */
    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getProfil(@PathVariable("userId") Long userId);
}