package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OffreClientFallback implements OffreClient {

    // Fallback for active offers: returns an empty list to avoid UI crashes.
    @Override
    public List<Map<String, Object>> getAllActive() {
        return new ArrayList<>();
    }

    // Fallback for offers by company: returns an empty list.
    @Override
    public List<Map<String, Object>> getByEntreprise(Long entrepriseId) {
        return new ArrayList<>();
    }

    // Fallback for creating an offer: returns an error map.
    @Override
    public Map<String, Object> create(Map<String, Object> offreData) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Impossible de créer l'offre. Le service est indisponible.");
        return fallback;
    }

    // Fallback for deleting an offer: silently logs or does nothing.
    @Override
    public void delete(Long id) {
        // Silent degradation: no action taken
    }

    // Fallback for fetching an offer by ID: returns a placeholder with limited info.
    @Override
    public Map<String, Object> getById(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("titre", "Détails indisponibles (Service Down)");
        return fallback;
    }

    // Fallback for updating an offer: returns an error map.
    @Override
    public Map<String, Object> update(Long id, Map<String, Object> offreData) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Impossible de modifier l'offre. Le service est indisponible.");
        return fallback;
    }
}