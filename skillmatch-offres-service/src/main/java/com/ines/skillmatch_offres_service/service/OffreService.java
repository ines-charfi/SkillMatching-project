package com.ines.skillmatch_offres_service.service;

import com.ines.skillmatch_offres_service.service.client.CandidatureClient;
import com.ines.skillmatch_offres_service.service.client.EntrepriseClient;
import com.ines.skillmatch_offres_service.service.client.NotificationClient;
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

// Service layer for job offer management. Handles CRUD operations, cross-service enrichment,
// and notification sending via Feign clients.
@Service
@RequiredArgsConstructor
@Slf4j
public class OffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseClient entrepriseClient;
    private final CandidatureClient candidatureClient;
    private final NotificationClient notificationClient;

    // Creates a new job offer, saves it to the database, and sends a notification to the admin.
    @Transactional
    public Offre create(OffreDTO dto) {
        Offre offre = Offre.builder()
                .entrepriseId(dto.getEntrepriseId())
                .userId(dto.getUserId())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .competencesRequises(dto.getCompetencesRequises())
                .niveauRequis(dto.getNiveauRequis())
                .salaire(dto.getSalaire())
                .active(true)
                .build();

        Offre savedOffre = offreRepository.save(offre);

        // Send notification to the admin about the new offer
        try {
            Map<String, Object> notifAdmin = new HashMap<>();
            notifAdmin.put("userIdTarget", 1L); // Fixed admin user ID
            notifAdmin.put("recipientRole", "admin");
            notifAdmin.put("type", "new_offre");
            notifAdmin.put("titreNotif", "Nouvelle offre publiée ");
            notifAdmin.put("message", "Une nouvelle offre intitulée '" + savedOffre.getTitre() + "' a été mise en ligne.");
            notifAdmin.put("lu", false);

            notificationClient.envoyerNotification(notifAdmin);
            log.info(" Notification de création d'offre transmise à l'administrateur.");
        } catch (Exception e) {
            // Isolated try-catch ensures offer creation is not blocked if auth/notification service is down.
            log.error(" Impossible de notifier l'admin pour la nouvelle offre : {}", e.getMessage());
        }

        return savedOffre;
    }

    // Updates an existing job offer by its ID.
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

    // Soft-deletes an offer by setting active = false (keeps historical data).
    @Transactional
    public void delete(Long id) {
        Offre offre = getById(id);
        offre.setActive(false);
        offreRepository.save(offre);
    }

    // Retrieves an offer by ID and enriches it with company info and application count.
    public Offre getById(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + id));
        enrichOffre(offre);
        return offre;
    }

    // Retrieves all active offers and enriches each one.
    public List<Offre> getAllActive() {
        List<Offre> offres = offreRepository.findByActiveTrue();
        offres.forEach(this::enrichOffre);
        return offres;
    }

    // Retrieves all active offers for a specific company and enriches them.
    public List<Offre> getByEntreprise(Long entrepriseId) {
        List<Offre> offres = offreRepository.findByEntrepriseIdAndActiveTrue(entrepriseId);
        offres.forEach(this::enrichOffre);
        return offres;
    }

    // Searches active offers by keyword in title, description, or required skills.
    public List<Offre> search(String keyword) {
        return offreRepository.searchOffres(keyword);
    }

    // Counts the total number of offers for a specific company.
    public long countByEntreprise(Long entrepriseId) {
        return offreRepository.countByEntrepriseId(entrepriseId);
    }

    // Retrieves the most recent offers (newest first) for homepage or sidebar.
    public List<Offre> getLatest() {
        return offreRepository.findLatestOffres();
    }

    // Returns the total count of all offers (used in the Admin Dashboard).
    public Long countAllOffres() {
        return offreRepository.count();
    }

    /**
     * Enriches an offer with additional data from other microservices via Feign clients.
     * Populates transient fields: entrepriseNom, entrepriseLogo, nombreCandidatures.
     */
    private void enrichOffre(Offre offre) {
        // 1. Fetch company details from the Entreprise service
        try {
            Map<String, Object> entreprise = entrepriseClient.getById(offre.getEntrepriseId());
            if (entreprise != null) {
                offre.setEntrepriseNom((String) entreprise.get("nomEntreprise"));
                offre.setEntrepriseLogo((String) entreprise.get("logoPath"));
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer l'entreprise pour l'offre {}: {}", offre.getId(), e.getMessage());
            offre.setEntrepriseNom("Entreprise inconnue");
        }

        // 2. Fetch application count from the Candidature service
        try {
            Long count = candidatureClient.CountByOffreId(offre.getId());
            offre.setNombreCandidatures(count != null ? count : 0L);
        } catch (Exception e) {
            log.warn("Impossible de compter les candidatures pour l'offre {}: {}", offre.getId(), e.getMessage());
            offre.setNombreCandidatures(0L);
        }
    }
}