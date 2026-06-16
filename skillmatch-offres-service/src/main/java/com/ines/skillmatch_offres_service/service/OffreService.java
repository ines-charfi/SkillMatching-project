package com.ines.skillmatch_offres_service.service;

import com.ines.skillmatch_offres_service.service.client.CandidatureClient;
import com.ines.skillmatch_offres_service.service.client.EntrepriseClient;
import com.ines.skillmatch_offres_service.service.client.NotificationClient; // 🎯 AJOUT : Import du client Feign
import com.ines.skillmatch_offres_service.dto.OffreDTO;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor // Génère le constructeur pour injecter les repos et clients automatiquement
@Slf4j // Pour les logs
public class OffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseClient entrepriseClient; // Client Feign
    private final CandidatureClient candidatureClient; // Client Feign
    private final NotificationClient notificationClient; // 🎯 AJOUT : Injection du client Notification

    @Transactional
    public Offre create(OffreDTO dto) {
        Offre offre = Offre.builder()
                .entrepriseId(dto.getEntrepriseId())
                .userId(dto.getUserId()) // Liaison indispensable pour tes notifs recruteur !
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .competencesRequises(dto.getCompetencesRequises())
                .niveauRequis(dto.getNiveauRequis())
                .salaire(dto.getSalaire())
                .active(true)
                .build();

        Offre savedOffre = offreRepository.save(offre);

        // 🎯 AJOUT : Notification envoyée à l'administrateur
        try {
            Map<String, Object> notifAdmin = new HashMap<>();
            notifAdmin.put("userIdTarget", 1L); // ID fixe de l'admin
            notifAdmin.put("recipientRole", "admin"); // Pour filtrer sur l'espace administration
            notifAdmin.put("type", "new_offre");
            notifAdmin.put("titreNotif", "Nouvelle offre publiée 💼");
            notifAdmin.put("message", "Une nouvelle offre intitulée '" + savedOffre.getTitre() + "' a été mise en ligne.");
            notifAdmin.put("lu", false);

            notificationClient.envoyerNotification(notifAdmin);
            log.info("🚀 Notification de création d'offre transmise à l'administrateur.");
        } catch (Exception e) {
            // Un bloc try-catch isolé évite de bloquer la création de l'offre si le service auth/notif est down.
            log.error("⚠️ Impossible de notifier l'admin pour la nouvelle offre : {}", e.getMessage());
        }

        return savedOffre;
    }

    @Transactional
    public Offre update(Long id, OffreDTO dto) {
        Offre offre = getById(id);
        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setCompetencesRequises(dto.getCompetencesRequises());
        offre.setNiveauRequis(dto.getNiveauRequis());
        offre.setSalaire(dto.getSalaire());
        return offreRepository.save(offre);
    }

    @Transactional
    public void delete(Long id) {
        Offre offre = getById(id);
        offre.setActive(false); // Soft delete pour garder l'historique
        offreRepository.save(offre);
    }

    public Offre getById(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + id));
        enrichOffre(offre);
        return offre;
    }

    public List<Offre> getAllActive() {
        List<Offre> offres = offreRepository.findByActiveTrue();
        offres.forEach(this::enrichOffre);
        return offres;
    }

    public List<Offre> getByEntreprise(Long entrepriseId) {
        List<Offre> offres = offreRepository.findByEntrepriseIdAndActiveTrue(entrepriseId);
        offres.forEach(this::enrichOffre);
        return offres;
    }

    public List<Offre> search(String keyword) {
        return offreRepository.searchOffres(keyword);
    }

    /**
     * Méthode d'enrichissement via OpenFeign
     * Remplit les champs @Transient pour le Frontend
     */
    private void enrichOffre(Offre offre) {

            // 1. Récupérer les infos de l'entreprise
            try {
                // CORRECTION ICI : On utilise getById au lieu de getEntrepriseByUserId
                Map<String, Object> entreprise = entrepriseClient.getById(offre.getEntrepriseId());
                if (entreprise != null) {
                    offre.setEntrepriseNom((String) entreprise.get("nomEntreprise"));
                    offre.setEntrepriseLogo((String) entreprise.get("logoPath"));
                }
            } catch (Exception e) {
                log.warn("Impossible de récupérer l'entreprise pour l'offre {}: {}", offre.getId(), e.getMessage());
                offre.setEntrepriseNom("Entreprise inconnue");
            }

        // 2. Récupérer le nombre de candidatures
        try {
            Long count = candidatureClient.CountByOffreId(offre.getId());
            offre.setNombreCandidatures(count != null ? count : 0L);
        } catch (Exception e) {
            log.warn("Impossible de compter les candidatures pour l'offre {}: {}", offre.getId(), e.getMessage());
            offre.setNombreCandidatures(0L);
        }
    }

    public long countByEntreprise(Long entrepriseId) {
        return offreRepository.countByEntrepriseId(entrepriseId);
    }

    public List<Offre> getLatest() {
        return offreRepository.findLatestOffres();
    }

    public Long countAllOffres() {
        return offreRepository.count();
    }
}