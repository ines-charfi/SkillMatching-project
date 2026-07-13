package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

// Fallback implementation for CandidatClient – called when the candidate service is unavailable.
@Component
public class CandidatClientFallback implements CandidatClient {

    // Fallback for profile initialization: throws an exception to prevent incomplete registration.
    @Override
    public void initCandidat(Long userId, String nom, String prenom) {
        throw new RuntimeException("Impossible d'initialiser le profil candidat. Le service candidat est indisponible.");
    }

    // Fallback for fetching candidate profile: returns dummy data with an error message.
    @Override
    public Map<String, Object> getCandidatByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nom", "Indisponible");
        fallback.put("prenom", "Profil");
        fallback.put("erreur", "Le service candidat est actuellement hors-ligne.");
        return fallback;
    }
}