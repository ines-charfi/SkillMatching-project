package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OffreClientFallback implements OffreClient {

    @Override
    public List<Map<String, Object>> getAllActive() {
        // Renvoie une liste vide si le service des offres est indisponible
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getByEntreprise(Long entrepriseId) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> offreData) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Impossible de créer l'offre. Le service est indisponible.");
        return fallback;
    }

    @Override
    public void delete(Long id) {
        // Log ou gestion silencieuse en mode dégradé
    }

    @Override
    public Map<String, Object> getById(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("titre", "Détails indisponibles (Service Down)");
        return fallback;
    }

    @Override
    public Map<String, Object> update(Long id, Map<String, Object> offreData) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Impossible de modifier l'offre. Le service est indisponible.");
        return fallback;
    }
}