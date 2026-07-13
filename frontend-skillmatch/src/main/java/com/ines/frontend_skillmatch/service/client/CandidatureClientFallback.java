package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CandidatureClientFallback implements CandidatureClient {

    // Fallback for applying: returns an error indicating the service is unavailable.
    @Override
    public Map<String, Object> postuler(Long candidatId, Long offreId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("success", false);
        fallback.put("error", "Le service de candidature est indisponible. Impossible de postuler pour le moment.");
        return fallback;
    }

    // Fallback for fetching applications by candidate: returns an empty list to avoid UI crashes.
    @Override
    public List<Map<String, Object>> getByCandidat(Long userId) {
        return new ArrayList<>();
    }

    // Fallback for fetching applications by company: returns an empty list to avoid UI crashes.
    @Override
    public List<Map<String, Object>> getByEntreprise(Long entrepriseId) {
        return new ArrayList<>();
    }

    // Fallback for company statistics: returns zeroed stats with a message.
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

    // Fallback for updating application status: throws an exception to inform the user.
    @Override
    public void updateStatut(Long id, String statut) {
        throw new RuntimeException("Impossible de modifier le statut de la candidature. Le service est hors-ligne.");
    }

    // Fallback for matching score: returns a default score of 0 if the AI/matching service is down.
    @Override
    public int getScore(Long userId, Long offreId) {
        return 0;
    }

    // Fallback for scheduling an interview: throws an exception indicating failure.
    @Override
    public void planifierEntretien(Long candidatureId, String dateStr, String lieu, String notes) {
        throw new RuntimeException("Échec de la planification de l'entretien. Le service de candidature ne répond pas.");
    }
}