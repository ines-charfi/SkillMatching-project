package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import com.ines.skillmatch_candidature_service.dto.CandidatureDTO;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final MatchingService matchingService;
    private final CandidatClient candidatClient;
    private final OffreClient offreClient;

    @Transactional
    public Candidature postuler(Long userId, Long offreId) {
        if (candidatureRepository.existsByCandidatIdAndOffreId(userId, offreId)) {
            throw new RuntimeException("Vous avez déjà postulé à cette offre");
        }
        try {
            Map<String, Object> candidat = candidatClient.getProfil(userId);
            Map<String, Object> offre = offreClient.getOffre(offreId);

            int score = matchingService.calculateScore(
                    (String) candidat.get("competences"),
                    (String) offre.get("competencesRequises"),
                    (String) candidat.get("niveauScolaire"),
                    (String) offre.get("niveauRequis")
            );

            Candidature candidature = Candidature.builder()
                    .candidatId(userId)
                    .offreId(offreId)
                    .scoreMatching(score)
                    .statut(Candidature.Statut.EN_ATTENTE)
                    .build();

            return candidatureRepository.save(candidature);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de communication inter-services");
        }
    }

    // MÉTHODE POUR LA MAQUETTE 4 (Tableau Entreprise)
    public List<CandidatureDTO> findAllByEntrepriseId(Long entrepriseId) {
        List<CandidatureDTO> results = new ArrayList<>();
        try {
            // 1. Récupérer les offres de l'entreprise
            List<Map<String, Object>> offres = offreClient.getOffresByEntreprise(entrepriseId);

            for (Map<String, Object> offre : offres) {
                Long oId = Long.valueOf(offre.get("id").toString());
                String titre = (String) offre.get("titre");

                // 2. Trouver les candidatures pour chaque offre
                List<Candidature> candList = candidatureRepository.findByOffreId(oId);

                for (Candidature c : candList) {
                    // 3. Récupérer le nom du candidat via Feign
                    Map<String, Object> candidat = candidatClient.getProfil(c.getCandidatId());
                    String nomComplet = candidat.get("prenom") + " " + candidat.get("nom");

                    results.add(CandidatureDTO.builder()
                            .id(c.getId())
                            .candidatId(c.getCandidatId())
                            .candidatNom(nomComplet)
                            .offreTitre(titre)
                            .scoreMatching(c.getScoreMatching())
                            .statut(c.getStatut().name())
                            .datePostulation(c.getDatePostulation())
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Erreur enrichissement candidatures: {}", e.getMessage());
        }
        return results;
    }

    public List<Candidature> getByCandidat(Long userId) {
        return candidatureRepository.findByCandidatId(userId);
    }

    public List<Candidature> getByOffre(Long offreId) {
        return candidatureRepository.findByOffreId(offreId);
    }

    public long countByOffre(Long offreId) {
        return candidatureRepository.countByOffreId(offreId);
    }

    public long countByCandidat(Long candidatId) {
        return candidatureRepository.countByCandidatId(candidatId);
    }

    @Transactional
    public Candidature updateStatut(Long id, String statut) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        candidature.setStatut(Candidature.Statut.valueOf(statut.toUpperCase()));
        return candidatureRepository.save(candidature);
    }

    public Map<String, Object> getStatsEntreprise(Long entrepriseId) {
        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> offres = offreClient.getOffresByEntreprise(entrepriseId);
        long total = 0;
        if (offres != null) {
            for (Map<String, Object> o : offres) {
                total += countByOffre(Long.valueOf(o.get("id").toString()));
            }
        }
        stats.put("totalCandidatures", total);
        stats.put("offresActives", offres != null ? offres.size() : 0);
        return stats;
    }
}