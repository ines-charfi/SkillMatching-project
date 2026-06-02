package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.model.Entretien;
import com.ines.skillmatch_candidature_service.repository.EntretienRepository;
import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import com.ines.skillmatch_candidature_service.dto.CandidatureDTO;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final EntretienRepository entretienRepository;
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


    // 🎯 MÉTHODE POUR CRÉER UN ENTRETIEN
    @Transactional
    public Entretien planifierEntretien(Long candidatureId, LocalDateTime date, String lieu, String notes) {
        // En option, on bascule le statut de la candidature en "ACCEPTE" ou un statut dédié si besoin
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

        Entretien entretien = Entretien.builder()
                .candidatureId(candidatureId)
                .dateEntretien(date)
                .lieu(lieu)
                .notes(notes)
                .statut(Entretien.Statut.PROGRAMME)
                .build();

        return entretienRepository.save(entretien);
    }

    // 🎯 MÉTHODE POUR RÉCUPÉRER LES ENTRETIENS D'UNE CANDIDATURE
    public List<Entretien> getEntretiensByCandidature(Long candidatureId) {
        return entretienRepository.findByCandidatureId(candidatureId);
    }

    // 🎯 ENRICHISSEMENT DES STATS POUR AFFICHER LE COMPTEUR SUR LE DASHBOARD
    public Map<String, Object> getStatsEntreprise(Long entrepriseId) {
        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> offres = offreClient.getOffresByEntreprise(entrepriseId);

        long totalCandidatures = 0;
        List<Long> candidatureIds = new ArrayList<>();

        if (offres != null) {
            for (Map<String, Object> o : offres) {
                Long oId = Long.valueOf(o.get("id").toString());
                List<Candidature> candList = candidatureRepository.findByOffreId(oId);
                totalCandidatures += candList.size();

                // On récupère tous les IDs de candidatures pour compter les entretiens
                for (Candidature c : candList) {
                    candidatureIds.add(c.getId());
                }
            }
        }

        // Calcul du nombre d'entretiens programmés (actifs)
        long entretiensPrevus = 0;
        if (!candidatureIds.isEmpty()) {
            entretiensPrevus = entretienRepository.countByCandidatureIdInAndStatut(candidatureIds, Entretien.Statut.PROGRAMME);
        }

        stats.put("totalCandidatures", totalCandidatures);
        stats.put("offresActives", offres != null ? offres.size() : 0);
        stats.put("entretiensPrevus", entretiensPrevus); // VALEUR POUR LA CASE DU DASHBOARD !
        return stats;
    }
}