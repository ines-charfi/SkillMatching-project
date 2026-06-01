package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final CandidatClient candidatClient;
    private final OffreClient offreClient;

    /**
     * Orchestre le calcul du score en récupérant les données via Feign
     */
    public int generateFullScore(Long userId, Long offreId) {
        try {
            // 1. Récupération des données microservices
            Map<String, Object> profil = candidatClient.getProfil(userId);
            Map<String, Object> offre = offreClient.getOffre(offreId);

            if (profil == null || offre == null) return 0;

            // 2. Extraction des données (selon ton script SQL)
            String candComp = (String) profil.get("competences");
            String candNiveau = (String) profil.get("niveauScolaire");

            String offreComp = (String) offre.get("competencesRequises");
            String offreNiveau = (String) offre.get("niveauRequis");

            // 3. Appel de ton algorithme de calcul
            return calculateScore(candComp, offreComp, candNiveau, offreNiveau);

        } catch (Exception e) {
            log.error("Erreur lors du calcul du matching : {}", e.getMessage());
            return 0;
        }
    }

    public int calculateScore(String candComp, String offreComp, String candNiv, String offreNiv) {
        double score = 0;

        // 1. Matching compétences (70%)
        if (candComp != null && offreComp != null && !offreComp.isEmpty()) {
            score += calculateCompetenceScore(candComp, offreComp) * 0.7;
        }

        // 2. Matching niveau d'études (30%)
        if (candNiv != null && offreNiv != null) {
            score += calculateNiveauScore(candNiv, offreNiv) * 0.3;
        }

        return (int) Math.min(Math.round(score), 100);
    }

    private double calculateCompetenceScore(String candComp, String offreComp) {
        Set<String> candidatSet = parseCompetences(candComp);
        Set<String> offreSet = parseCompetences(offreComp);

        if (offreSet.isEmpty()) return 0;

        long matches = offreSet.stream()
                .filter(candidatSet::contains)
                .count();

        return ((double) matches / offreSet.size()) * 100;
    }

    private double calculateNiveauScore(String candNiv, String offreNiv) {
        int nivCand = normaliserNiveau(candNiv);
        int nivOffre = normaliserNiveau(offreNiv);

        if (nivCand >= nivOffre) return 100; // Profil égal ou supérieur
        if (nivCand == nivOffre - 1) return 50; // Un niveau en dessous (ex: Bac+4 pour Bac+5)
        return 0;
    }

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