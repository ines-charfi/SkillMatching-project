package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CandidatClientFallback implements CandidatClient {

    @Override
    public void initCandidat(Long userId, String nom, String prenom) {
        // Mode secours : On jette une exception pour empêcher la finalisation de l'inscription
        // si le microservice candidat ne peut pas être initialisé.
        throw new RuntimeException("Impossible d'initialiser le profil candidat. Le service candidat est indisponible.");
    }

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