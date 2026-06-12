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

    @Transactional
    public Candidature postuler(Long userId, Long offreId) {
        if (candidatureRepository.existsByCandidatIdAndOffreId(userId, offreId)) {
            throw new RuntimeException("Vous avez déjà postulé à cette offre");
        }
        try {
            Map<String, Object> candidat = candidatClient.getProfil(userId);
            Map<String, Object> offre = offreClient.getOffre(offreId);

            // 🎯 LOGS DE DEBUG : Pour traquer la structure exacte de l'offre reçue en cas de problème
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

                    // 🎯 RECTIFICATION DOUBLE SÉCURITÉ (userId vs user_id)
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

                    Map<String, Object> notifData = new HashMap<>();
                    notifData.put("userIdTarget", recruteurUserId);
                    notifData.put("recipientRole", "recruiter"); // Rôle de l'espace entreprise
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
            notifData.put("recipientRole", "candidate"); // Identifié pour l'espace Candidat uniquement
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

    public List<Candidature> getByCandidat(Long userId) { return candidatureRepository.findByCandidatId(userId); }
    public List<Candidature> getByOffre(Long offreId) { return candidatureRepository.findByOffreId(offreId); }
    public long countByOffre(Long offreId) { return candidatureRepository.countByOffreId(offreId); }
    public long countByCandidat(Long candidatId) { return candidatureRepository.countByCandidatId(candidatId); }

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