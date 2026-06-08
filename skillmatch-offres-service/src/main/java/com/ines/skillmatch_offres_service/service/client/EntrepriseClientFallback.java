package com.ines.skillmatch_offres_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntrepriseClientFallback implements EntrepriseClient {

    @Override
    public Map<String, Object> getEntrepriseByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nomEntreprise", "Service Entreprise Indisponible");
        fallback.put("statut", "DOWN");
        return fallback;
    }

    @Override
    public Map<String, Object> getById(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("nomEntreprise", "Détails entreprise indisponibles");
        fallback.put("statut", "DOWN");
        return fallback;
    }
}