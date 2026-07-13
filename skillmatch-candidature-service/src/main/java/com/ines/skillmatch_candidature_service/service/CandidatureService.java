package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import com.ines.skillmatch_candidature_service.service.client.NotificationClient;
import com.ines.skillmatch_candidature_service.dto.CandidatureDTO;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service class handling the business logic for job applications (Candidatures).
 * Coordinates data from Candidate, Job Offer, and Notification microservices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final MatchingService matchingService;
    private final CandidatClient candidatClient;
    private final OffreClient offreClient;
    private final NotificationClient notificationClient;
    private final com.ines.skillmatch_candidature_service.repository.EntretienRepository entretienRepository;

    /**
     * Orchestrates the application process:
     * 1. Checks for existing applications.
     * 2. Fetches Candidate and Job details via OpenFeign.
     * 3. Calculates the matching score.
     * 4. Persists the application.
     * 5. Sends a real-time notification to the recruiter.
     */
    @Transactional
    public Candidature postuler(Long userId, Long offreId) {
        if (candidatureRepository.existsByCandidatIdAndOffreId(userId, offreId)) {
            throw new RuntimeException("Vous avez déjà postulé à cette offre");
        }
        try {
            Map<String, Object> candidat = candidatClient.getProfil(userId);
            Map<String, Object> offre = offreClient.getOffre(offreId);

            // DEBUG LOGS: To track the exact structure of the job offer received via Feign
            log.info("🔍 [DEBUG NOTIF] Offre récupérée via Feign : {}", offre);

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

            Candidature savedCandidature = candidatureRepository.save(candidature);

            try {
                if (offre != null && offre.get("entrepriseId") != null) {
                    Long entId = Long.valueOf(offre.get("entrepriseId").toString());

                    // Double security check for recruiter User ID mapping
                    Long recruteurUserId = null;
                    if (offre.get("userId") != null) {
                        recruteurUserId = Long.valueOf(offre.get("userId").toString());
                    } else if (offre.get("user_id") != null) {
                        recruteurUserId = Long.valueOf(offre.get("user_id").toString());
                    } else {
                        log.warn("⚠️ Pas de userId trouvé dans l'offre. Utilisation de l'ID Entreprise (entId) en secours.");
                        recruteurUserId = entId;
                    }

                    String prenomCand = candidat.get("prenom") != null ? candidat.get("prenom").toString() : "";
                    String nomCand = candidat.get("nom") != null ? candidat.get("nom").toString() : "Un candidat";
                    String titreOffre = offre.get("titre") != null ? offre.get("titre").toString() : "votre offre";

                    // Prepare notification payload for the recruiter
                    Map<String, Object> notifData = new HashMap<>();
                    notifData.put("userIdTarget", recruteurUserId);
                    notifData.put("recipientRole", "recruiter");
                    notifData.put("type", "new_application");
                    notifData.put("titreNotif", "Nouvelle candidature reçue ! 📩");
                    notifData.put("message", prenomCand + " " + nomCand + " a postulé pour le poste : " + titreOffre + " (Score Matching : " + score + "%)");
                    notifData.put("lu", false);

                    log.info("🚀 Envoi de la notification au Recruteur User ID cible : {}", recruteurUserId);
                    notificationClient.envoyerNotification(notifData);

                } else {
                    log.warn("⚠️ Impossible d'envoyer la notification : entrepriseId introuvable dans l'offre.");
                }
            } catch (Exception ex) {
                log.error("❌ Échec lors de l'envoi de la notification au recruteur : {}", ex.getMessage());
            }

            return savedCandidature;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de communication inter-services : " + e.getMessage());
        }
    }

    /**
     * Updates the application status and sends a tailored notification to the candidate
     * based on the new status (Accepted, Rejected, Interview).
     */
    @Transactional
    public Candidature updateStatut(Long id, String statut) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        candidature.setStatut(Candidature.Statut.valueOf(statut.toUpperCase()));
        Candidature updatedCandidature = candidatureRepository.save(candidature);

        try {
            Long candidatUserId = updatedCandidature.getCandidatId();
            Map<String, Object> offre = offreClient.getOffre(updatedCandidature.getOffreId());
            String titreOffre = offre.get("titre") != null ? offre.get("titre").toString() : "votre candidature";

            String titreNotif = "Mise à jour de votre candidature 📋";
            String message = "";

            // Dynamic notification content based on workflow state
            switch (statut.toUpperCase()) {
                case "ACCEPTE":
                    titreNotif = "Candidature Acceptée ! 🎉";
                    message = "Excellente nouvelle ! Votre candidature pour le poste de \"" + titreOffre + "\" a été acceptée par le recruteur.";
                    break;
                case "REFUSE":
                    titreNotif = "Retour sur votre candidature 📨";
                    message = "Malheureusement, votre profil n'a pas été retenu pour le poste de \"" + titreOffre + "\". Ne découragez pas, d'autres opportunités vous attendent !";
                    break;
                case "ENTRETIEN":
                    titreNotif = "Invitation à un entretien ! 🗓️";
                    message = "Bonne nouvelle ! Le recruteur souhaite planifier un entretien avec vous pour le poste de \"" + titreOffre + "\".";
                    break;
                default:
                    message = "Le statut de votre candidature pour le poste de \"" + titreOffre + "\" a été mis à jour : " + statut;
                    break;
            }

            Map<String, Object> notifData = new HashMap<>();
            notifData.put("userIdTarget", candidatUserId);
            notifData.put("recipientRole", "candidate");
            notifData.put("titreNotif", titreNotif);
            notifData.put("message", message);
            notifData.put("lu", false);

            notificationClient.envoyerNotification(notifData);
            log.info("🚀 Notification de suivi envoyée avec succès au candidat ID {}", candidatUserId);

        } catch (Exception ex) {
            log.error("⚠️ Impossible d'envoyer la notification de statut au candidat : {}", ex.getMessage());
        }

        return updatedCandidature;
    }

    /**
     * Retrieves and enriches job applications for a company.
     * Iterates through company offers and fetches associated candidates to build DTOs.
     */
    public List<CandidatureDTO> findAllByEntrepriseId(Long entrepriseId) {
        List<CandidatureDTO> results = new ArrayList<>();
        try {
            List<Map<String, Object>> offres = offreClient.getOffresByEntreprise(entrepriseId);
            for (Map<String, Object> offre : offres) {
                Long oId = Long.valueOf(offre.get("id").toString());
                String titre = (String) offre.get("titre");
                List<Candidature> candList = candidatureRepository.findByOffreId(oId);
                for (Candidature c : candList) {
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
        } catch (Exception e) { log.error("Erreur enrichissement candidatures: {}", e.getMessage()); }
        return results;
    }

    // Access methods for basic application retrieval and statistics
    public List<Candidature> getByCandidat(Long userId) { return candidatureRepository.findByCandidatId(userId); }
    public List<Candidature> getByOffre(Long offreId) { return candidatureRepository.findByOffreId(offreId); }
    public long countByOffre(Long offreId) { return candidatureRepository.countByOffreId(offreId); }
    public long countByCandidat(Long candidatId) { return candidatureRepository.countByCandidatId(candidatId); }

    /**
     * Aggregates recruitment statistics for a specific company's dashboard.
     */
    public Map<String, Object> getStatsEntreprise(Long entrepriseId) {
        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> offres = offreClient.getOffresByEntreprise(entrepriseId);
        long totalCandidatures = 0; long totalEntretiens = 0;
        if (offres != null) {
            for (Map<String, Object> o : offres) {
                Long oId = Long.valueOf(o.get("id").toString());
                totalCandidatures += countByOffre(oId);
                totalEntretiens += entretienRepository.countByCandidatureIdIn(
                        candidatureRepository.findByOffreId(oId).stream().map(Candidature::getId).toList()
                );
            }
        }
        stats.put("totalCandidatures", totalCandidatures);
        stats.put("offresActives", offres != null ? offres.size() : 0);
        stats.put("entretiensCount", totalEntretiens);
        stats.put("totalEntretiens", totalEntretiens);
        return stats;
    }
}