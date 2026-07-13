package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fallback implementation for OffreClient – called when the offer service is unavailable.
@Component
public class OffreClientFallback implements OffreClient {

    // Fallback for fetching a single offer by ID: returns a dummy offer with a maintenance message.
    @Override
    public Map<String, Object> getOffre(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("titre", "Offre indisponible temporairement");
        fallback.put("entreprise", "SkillMatch Security Mode");
        fallback.put("statut", "MAINTENANCE");
        return fallback;
    }

    // Fallback for fetching offers by company: returns an empty list to avoid breaking the frontend display.
    @Override
    public List<Map<String, Object>> getOffresByEntreprise(Long entrepriseId) {
        return new ArrayList<>();
    }
}