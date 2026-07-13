package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback implementation for CandidatClient using the Circuit Breaker pattern.
 * This class provides a default response if the 'skillmatch-candidat-service'
 * microservice is unreachable, down, or experiencing high latency.
 */
@Component
public class CandidatClientFallback implements CandidatClient {

    /**
     * Returns a mock profile response when the remote service call fails.
     * Ensures graceful degradation of the system by providing placeholder data
     * instead of allowing the application to crash or throw an exception.
     *
     * @param id The identifier of the user whose profile was requested
     * @return A map containing default values indicating service unavailability
     */
    @Override
    public Map<String, Object> getProfil(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("nom", "Utilisateur");
        fallback.put("prenom", "Indisponible");
        fallback.put("competences", "Données indisponibles (Mode secours actif)");
        return fallback;
    }
}