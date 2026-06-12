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
@Slf4j // 🎯 AJOUT : Pour gérer proprement les messages de logs
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final NotificationClient notificationClient; // 🎯 AJOUT : Injection du client Feign pour l'envoi de notifs

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // 1. RÉCUPÉRATION PAR ID
    public Candidat getById(Long id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + id));
    }

    // 2. RÉCUPÉRATION PAR USER_ID
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

    // 4. VALIDATION ADMIN
    @Transactional
    public Candidat updateValidationStatus(Long id, Candidat.ValidationStatut statut) {
        Candidat candidat = this.getById(id);
        candidat.setValidationStatut(statut);
        return candidatRepository.save(candidat);
    }

    // 5. RECHERCHE
    public List<Candidat> searchByCompetence(String competence) {
        return candidatRepository.findByCompetence(competence);
    }

    public List<Candidat> getAll() {
        return candidatRepository.findAll();
    }

    // 🎯 FIX SÉCURITÉ STOCKAGE : Évite l'accumulation d'UUID en cascade
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
            hasNewCv = true; // 🎯 Le drapeau passe à true
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
                notifAdmin.put("titreNotif", "Nouveau document à vérifier 📄");
                notifAdmin.put("message", savedCandidat.getPrenom() + " " + savedCandidat.getNom() + " a ajouté ou modifié son CV. Une analyse est requise.");
                notifAdmin.put("lu", false);

                notificationClient.envoyerNotification(notifAdmin);
                log.info("🚀 Notification de nouveau CV transmise à l'administrateur.");
            } catch (Exception ex) {
                // Protège le candidat : le profil reste enregistré même si l'auth-service/notif ne répond pas
                log.error("⚠️ Impossible de notifier l'admin pour le nouveau CV : {}", ex.getMessage());
            }
        }

        return savedCandidat;
    }
}