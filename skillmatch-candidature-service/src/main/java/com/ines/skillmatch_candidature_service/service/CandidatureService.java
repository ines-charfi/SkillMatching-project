package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final MatchingService matchingService;
    private final RestTemplate restTemplate;

    public CandidatureService(CandidatureRepository candidatureRepository,
                              MatchingService matchingService,
                              RestTemplate restTemplate) {
        this.candidatureRepository = candidatureRepository;
        this.matchingService = matchingService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Candidature postuler(Long candidatId, Long offreId) {
        if (candidatureRepository.existsByCandidatIdAndOffreId(candidatId, offreId)) {
            throw new RuntimeException("Vous avez déjà postulé à cette offre");
        }

        // Récupérer les infos
        Map candidat = getCandidatInfo(candidatId);
        Map offre = getOffreInfo(offreId);

        // Calculer le score
        int score = matchingService.calculateScore(
                (String) candidat.get("competences"),
                (String) offre.get("competencesRequises"),
                (String) candidat.get("niveauScolaire"),
                (String) offre.get("niveauRequis")
        );

        Candidature candidature = Candidature.builder()
                .candidatId(candidatId)
                .offreId(offreId)
                .scoreMatching(score)
                .statut(Candidature.Statut.EN_ATTENTE)
                .build();

        return candidatureRepository.save(candidature);
    }

    public List<Candidature> getByCandidat(Long candidatId) {
        return candidatureRepository.findByCandidatId(candidatId);
    }

    public List<Candidature> getByOffre(Long offreId) {
        return candidatureRepository.findByOffreId(offreId);
    }

    @Transactional
    public Candidature updateStatut(Long id, String statut) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        candidature.setStatut(Candidature.Statut.valueOf(statut.toUpperCase()));
        return candidatureRepository.save(candidature);
    }

    public long countByOffre(Long offreId) {
        return candidatureRepository.countByOffreId(offreId);
    }

    public long countByCandidat(Long candidatId) {
        return candidatureRepository.countByCandidatId(candidatId);
    }

    public Map<String, Object> getStatsEntreprise(Long entrepriseId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            List offres = restTemplate.getForObject(
                    "http://offre-service/api/offres/entreprise/" + entrepriseId, List.class);

            long total = 0;
            if (offres != null) {
                for (Object o : offres) {
                    Map offre = (Map) o;
                    Long offreId = ((Number) offre.get("id")).longValue();
                    total += candidatureRepository.countByOffreId(offreId);
                }
            }
            stats.put("totalCandidatures", total);
            stats.put("offresActives", offres != null ? offres.size() : 0);
        } catch (Exception e) {
            stats.put("totalCandidatures", 0);
            stats.put("offresActives", 0);
        }
        return stats;
    }

    private Map getCandidatInfo(Long candidatId) {
        try {
            return restTemplate.getForObject(
                    "http://candidat-service/api/candidats/" + candidatId, Map.class);
        } catch (Exception e) {
            return Map.of("competences", "", "niveauScolaire", "");
        }
    }

    private Map getOffreInfo(Long offreId) {
        try {
            return restTemplate.getForObject(
                    "http://offre-service/api/offres/" + offreId, Map.class);
        } catch (Exception e) {
            return Map.of("competencesRequises", "", "niveauRequis", "");
        }
    }
}