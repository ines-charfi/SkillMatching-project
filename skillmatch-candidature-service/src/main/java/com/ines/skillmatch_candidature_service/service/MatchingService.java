package com.ines.skillmatch_candidature_service.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MatchingService {

    /**
     * Calcule le score de matching entre un candidat et une offre
     */
    public int calculateScore(String candidatCompetences, String offreCompetences,
                              String candidatNiveau, String offreNiveau) {
        double score = 0;

        // Matching compétences (70%)
        if (candidatCompetences != null && offreCompetences != null) {
            score += calculateCompetenceScore(candidatCompetences, offreCompetences) * 0.7;
        }

        // Matching niveau (30%)
        if (candidatNiveau != null && offreNiveau != null) {
            score += calculateNiveauScore(candidatNiveau, offreNiveau) * 0.3;
        }

        return Math.min((int) score, 100);
    }

    private double calculateCompetenceScore(String candidatComp, String offreComp) {
        Set<String> candidatSet = parseCompetences(candidatComp);
        Set<String> offreSet = parseCompetences(offreComp);

        if (offreSet.isEmpty()) return 0;

        int matches = 0;
        for (String comp : offreSet) {
            if (candidatSet.contains(comp.toLowerCase())) {
                matches++;
            }
        }

        return ((double) matches / offreSet.size()) * 100;
    }

    private double calculateNiveauScore(String candidatNiveau, String offreNiveau) {
        int nivCandidat = normaliserNiveau(candidatNiveau);
        int nivOffre = normaliserNiveau(offreNiveau);

        if (nivCandidat >= nivOffre) return 100;
        if (nivCandidat == nivOffre - 1) return 50;
        return 0;
    }

    private int normaliserNiveau(String niveau) {
        String n = niveau.toLowerCase();
        if (n.contains("bac+8") || n.contains("doctorat")) return 8;
        if (n.contains("bac+5") || n.contains("master")) return 5;
        if (n.contains("bac+4")) return 4;
        if (n.contains("bac+3") || n.contains("licence")) return 3;
        if (n.contains("bac+2")) return 2;
        if (n.contains("bac")) return 1;
        return 0;
    }

    private Set<String> parseCompetences(String competences) {
        Set<String> result = new HashSet<>();
        for (String c : competences.split(",")) {
            String trimmed = c.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}