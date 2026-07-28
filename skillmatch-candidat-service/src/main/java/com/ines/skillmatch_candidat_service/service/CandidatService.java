package com.ines.skillmatch_candidat_service.service;

import com.ines.skillmatch_candidat_service.dto.CandidatDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.repository.CandidatRepository;
import com.ines.skillmatch_candidat_service.service.client.NotificationClient; // 🎯 AJOUT : Import du client de notification
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 🎯 AJOUT : Pour les logs
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j // AJOUT : Pour gérer proprement les messages de logs
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final NotificationClient notificationClient; //  Injection of client Feign to send the notifs
    /**
     * Directory where uploaded files (CVs and photos) are stored.
     * Injected from application.properties (default: "uploads").
     */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Retrieves a candidate by its internal database ID.
     *
     * @param id the candidate's primary key
     * @return the Candidat entity
     * @throws RuntimeException if no candidate is found with that ID
     */
    public Candidat getById(Long id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + id));
    }
    /**
     * Retrieves a candidate by the authentication user ID.
     * If no profile exists for the given userId, it creates a default one
     * (with placeholder data) and returns it.
     *
     * This method is transactional to ensure that the creation and retrieval
     * happen in a single database session.
     *
     * @param userId the user ID from the auth service
     * @return the existing or newly created Candidat profile
     */
    @Transactional
    public Candidat getByUserId(Long userId) {
        return candidatRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("⚠️ Aucun candidat trouvé pour userId {}. Création d'un profil par défaut...", userId);
                    Candidat nouveauCandidat = Candidat.builder()
                            .userId(userId)
                            .nom("Candidat")
                            .prenom("Nouveau")
                            .bio("Complétez votre bio pour attirer les recruteurs.")
                            .validationStatut(Candidat.ValidationStatut.EN_ATTENTE)
                            .build();
                    return candidatRepository.save(nouveauCandidat);
                });
    }

    // 3. INITIALISATION (Feign)
    /**
     * Initializes a candidate profile for a new user.
     * Only creates the profile if it does not already exist.
     *
     * @param userId the authentication user ID
     * @param nom    the candidate's last name
     * @param prenom the candidate's first name
     */
    @Transactional
    public void initCandidat(Long userId, String nom, String prenom) {
        if (candidatRepository.findByUserId(userId).isEmpty()) {
            Candidat c = Candidat.builder()
                    .userId(userId)
                    .nom(nom)
                    .prenom(prenom)
                    .validationStatut(Candidat.ValidationStatut.EN_ATTENTE)
                    .build();
            candidatRepository.save(c);
        }
    }

    // 4. VALIDATION OF ADMIN
    /**
     * Updates the validation status of a candidate profile.
     * This is typically called by an administrator to approve or reject a profile.
     *
     * @param id     the candidate's internal ID
     * @param statut the new validation status (EN_ATTENTE, VALIDE, REJETE)
     * @return the updated Candidat entity
     */
    @Transactional
    public Candidat updateValidationStatus(Long id, Candidat.ValidationStatut statut) {
        Candidat candidat = this.getById(id);
        candidat.setValidationStatut(statut);
        return candidatRepository.save(candidat);
    }

    /**
     * Searches for candidates whose skills (competences) contain the given keyword.
     *
     * @param competence the skill to search for (e.g., "Java")
     * @return a list of matching candidates
     */
    public List<Candidat> searchByCompetence(String competence) {
        return candidatRepository.findByCompetence(competence);
    }
    /**
     * Retrieves all candidate profiles.
     *
     * @return a list of all candidates
     */
    public List<Candidat> getAll() {
        return candidatRepository.findAll();
    }

    //  FIX SÉCURITY OF  STOCKAGE : Évite l'accumulation d'UUID en cascade
    /**
     * Saves an uploaded file (CV or photo) to the filesystem.
     * Generates a unique filename using UUID to avoid collisions.
     * Creates the target directory if it does not exist.
     *
     * Security note: The UUID-based naming prevents path traversal attacks
     * and avoids overwriting existing files.
     *
     * @param file   the MultipartFile to save
     * @param subDir the subdirectory under the base upload directory (e.g., "cvs" or "photos")
     * @return the generated filename (not the full path)
     * @throws IOException if file I/O fails
     */
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path uploadPath = Paths.get(uploadDir, subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
    // ============================
    // 6. MAIN PROFILE UPDATE (with file uploads and admin notification)
    // ============================

    /**
     * Updates a candidate's profile with new information and optional file uploads.
     * This is the core method called by the controller's update endpoint.
     *
     * The method:
     * - Updates all textual fields from the DTO.
     * - Saves new CV and/or photo files if provided.
     * - If a new CV is uploaded, it sends a notification to an administrator
     *   via the NotificationClient (to alert them of a new document to review).
     *
     * The notification is sent asynchronously; if it fails, the profile update
     * is still committed (the operation is protected by a try-catch).
     *
     * @param userId the authentication user ID
     * @param dto    the DTO containing the updated profile data
     * @param cv     the CV file (MultipartFile) – optional
     * @param photo  the profile photo file (MultipartFile) – optional
     * @return the saved Candidat entity
     * @throws IOException if file saving fails
     */
    // 6. MISE À JOUR PRINCIPALE DU PROFIL (Appelée par ton contrôleur)
    @Transactional
    public Candidat updateProfilWithFile(Long userId, CandidatDTO dto, MultipartFile cv, MultipartFile photo) throws IOException {

        Candidat candidatExistant = candidatRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Candidat introuvable pour le user ID : " + userId));

        candidatExistant.setPrenom(dto.getPrenom());
        candidatExistant.setNom(dto.getNom());
        candidatExistant.setTelephone(dto.getTelephone());
        candidatExistant.setAdresse(dto.getAdresse());
        candidatExistant.setBio(dto.getBio());
        candidatExistant.setCompetences(dto.getCompetences());
        candidatExistant.setLinkedinUrl(dto.getLinkedinUrl());
        candidatExistant.setPortfolioUrl(dto.getPortfolioUrl());
        candidatExistant.setNiveauScolaire(dto.getNiveauScolaire());

        boolean hasNewCv = false;

        // Sauvegarde du CV s'il y en a un nouveau fourni
        if (cv != null && !cv.isEmpty()) {
            String cvName = saveFile(cv, "cvs");
            candidatExistant.setCvPath(cvName);
            hasNewCv = true; //  Le drapeau passe à true
        }

        // Sauvegarde de la Photo s'il y en a une nouvelle fournie
        if (photo != null && !photo.isEmpty()) {
            String photoName = saveFile(photo, "photos");
            candidatExistant.setPhotoPath(photoName);
        }

        Candidat savedCandidat = candidatRepository.save(candidatExistant);

        // 🎯 AJOUT : Si un nouveau CV est ajouté/modifié, on envoie la notification à l'admin
        if (hasNewCv) {
            try {
                Map<String, Object> notifAdmin = new HashMap<>();
                notifAdmin.put("userIdTarget", 1L); // ID fixe de ton administrateur
                notifAdmin.put("recipientRole", "admin"); // Pour cibler son Dashboard
                notifAdmin.put("type", "new_cv");
                notifAdmin.put("titreNotif", "Nouveau document à vérifier ");
                notifAdmin.put("message", savedCandidat.getPrenom() + " " + savedCandidat.getNom() + " a ajouté ou modifié son CV. Une analyse est requise.");
                notifAdmin.put("lu", false);

                notificationClient.envoyerNotification(notifAdmin);
                log.info(" Notification de nouveau CV transmise à l'administrateur.");
            } catch (Exception ex) {
                // Protège le candidat : le profil reste enregistré même si l'auth-service/notif ne répond pas
                log.error("⚠ Impossible de notifier l'admin pour le nouveau CV : {}", ex.getMessage());
            }
        }

        return savedCandidat;
    }
}