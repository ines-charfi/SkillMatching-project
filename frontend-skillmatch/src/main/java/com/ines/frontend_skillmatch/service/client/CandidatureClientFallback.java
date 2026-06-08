package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CandidatureClientFallback implements CandidatureClient {

    @Override
    public Map<String, Object> postuler(Long candidatId, Long offreId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("success", false);
        fallback.put("error", "Le service de candidature est indisponible. Impossible de postuler pour le moment.");
        return fallback;
    }

    @Override
    public List<Map<String, Object>> getByCandidat(Long userId) {
        // Évite le crash du tableau de bord du candidat en renvoyant une liste vide
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getByEntreprise(Long entrepriseId) {
        // Évite le crash de l'espace Recruteur en renvoyant une liste vide
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getStatsEntreprise(Long entrepriseId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("totalCandidatures", 0);
        fallback.put("enAttente", 0);
        fallback.put("acceptees", 0);
        fallback.put("refusees", 0);
        fallback.put("msg", "Données statistiques indisponibles");
        return fallback;
    }

    @Override
    public void updateStatut(Long id, String statut) {
        throw new RuntimeException("Impossible de modifier le statut de la candidature. Le service est hors-ligne.");
    }

    @Override
    public int getScore(Long userId, Long offreId) {
        // Renvoie un score par défaut de 0 si le moteur de matching/IA est injoignable
        return 0;
    }

    @Override
    public void planifierEntretien(Long candidatureId, String dateStr, String lieu, String notes) {
        throw new RuntimeException("Échec de la planification de l'entretien. Le service de candidature ne répond pas.");
    }
}