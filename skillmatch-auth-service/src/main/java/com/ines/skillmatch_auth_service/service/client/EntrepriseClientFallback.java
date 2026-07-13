package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

// Fallback implementation for EntrepriseClient – called when the enterprise service is unavailable.
@Component
public class EntrepriseClientFallback implements EntrepriseClient {

    // Fallback for company initialization: throws an exception to prevent incomplete registration.
    @Override
    public void initEntreprise(Long userId, String nom) {
        throw new RuntimeException("Impossible d'initialiser le compte entreprise. Le service entreprise est indisponible.");
    }

    // Fallback for fetching company profile: returns dummy data with an error message.
    @Override
    public Map<String, Object> getEntrepriseByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nomEntreprise", "Indisponible");
        fallback.put("erreur", "Le service entreprise est actuellement hors-ligne.");
        return fallback;
    }
}