package com.ines.skillmatch_offres_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

// Fallback implementation for EntrepriseClient – called when the enterprise service is unavailable.
@Component
public class EntrepriseClientFallback implements EntrepriseClient {

    // Fallback for fetching company by user ID: returns dummy data with a "DOWN" status.
    @Override
    public Map<String, Object> getEntrepriseByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nomEntreprise", "Service Entreprise Indisponible");
        fallback.put("statut", "DOWN");
        return fallback;
    }

    // Fallback for fetching company by internal ID: returns dummy data with a "DOWN" status.
    @Override
    public Map<String, Object> getById(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("nomEntreprise", "Détails entreprise indisponibles");
        fallback.put("statut", "DOWN");
        return fallback;
    }
}