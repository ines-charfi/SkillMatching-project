package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Service dedicated to the core matching algorithm.
 * It calculates the compatibility between a candidate's profile and a job offer
 * based on weighted technical skills and education levels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final CandidatClient candidatClient;
    private final OffreClient offreClient;

    /**
     * Orchestrates the scoring process by retrieving data from remote microservices via Feign.
     *
     * @param userId ID of the candidate
     * @param offreId ID of the job offer
     * @return The final matching score as an integer (0 to 100)
     */
    public int generateFullScore(Long userId, Long offreId) {
        try {
            // 1. Fetch data from external microservices
            Map<String, Object> profil = candidatClient.getProfil(userId);
            Map<String, Object> offre = offreClient.getOffre(offreId);

            if (profil == null || offre == null) return 0;

            // 2. Extract specific fields from the generic Map responses
            String candComp = (String) profil.get("competences");
            String candNiveau = (String) profil.get("niveauScolaire");

            String offreComp = (String) offre.get("competencesRequises");
            String offreNiveau = (String) offre.get("niveauRequis");

            // 3. Execute the matching calculation
            return calculateScore(candComp, offreComp, candNiveau, offreNiveau);

        } catch (Exception e) {
            log.error("Error during matching calculation: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Main calculation method using a weighted formula.
     * Weights: 70% for technical skills, 30% for education level.
     */
    public int calculateScore(String candComp, String offreComp, String candNiv, String offreNiv) {
        double score = 0;

        // 1. Technical skills matching (70% of the total score)
        if (candComp != null && offreComp != null && !offreComp.isEmpty()) {
            score += calculateCompetenceScore(candComp, offreComp) * 0.7;
        }

        // 2. Education level matching (30% of the total score)
        if (candNiv != null && offreNiv != null) {
            score += calculateNiveauScore(candNiv, offreNiv) * 0.3;
        }

        // Round the result and cap it at 100%
        return (int) Math.min(Math.round(score), 100);
    }

    /**
     * Calculates skill matching using an intersection-based approach (Jaccard similarity style).
     * Compares how many of the required skills the candidate possesses.
     */
    private double calculateCompetenceScore(String candComp, String offreComp) {
        Set<String> candidatSet = parseCompetences(candComp);
        Set<String> offreSet = parseCompetences(offreComp);

        if (offreSet.isEmpty()) return 0;

        // Count how many required skills are present in the candidate's profile
        long matches = offreSet.stream()
                .filter(candidatSet::contains)
                .count();

        return ((double) matches / offreSet.size()) * 100;
    }

    /**
     * Compares standardized education levels.
     * Returns 100% if the candidate meets or exceeds the requirement.
     * Returns 50% if the candidate is exactly one level below (e.g., Bac+4 for a Bac+5 job).
     */
    private double calculateNiveauScore(String candNiv, String offreNiv) {
        int nivCand = normaliserNiveau(candNiv);
        int nivOffre = normaliserNiveau(offreNiv);

        if (nivCand >= nivOffre) return 100; // Profile meets or exceeds requirements
        if (nivCand == nivOffre - 1) return 50; // Profile is close (one year/level difference)
        return 0;
    }

    /**
     * Converts natural language education strings into comparable numeric ranks.
     */
    private int normaliserNiveau(String niveau) {
        if (niveau == null) return 0;
        String n = niveau.toLowerCase();
        if (n.contains("bac+8") || n.contains("doctorat")) return 8;
        if (n.contains("bac+5") || n.contains("master") || n.contains("ingénieur")) return 5;
        if (n.contains("bac+4")) return 4;
        if (n.contains("bac+3") || n.contains("licence")) return 3;
        if (n.contains("bac+2")) return 2;
        if (n.contains("bac")) return 1;
        return 0;
    }

    /**
     * Utility method to transform comma-separated strings into a cleaned set of unique keywords.
     * Handles case-insensitivity and whitespace trimming.
     */
    private Set<String> parseCompetences(String input) {
        if (input == null || input.isEmpty()) return Collections.emptySet();
        Set<String> result = new HashSet<>();
        for (String c : input.split(",")) {
            String trimmed = c.trim().toLowerCase();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }
}