package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CandidatClientFallback implements CandidatClient {

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
