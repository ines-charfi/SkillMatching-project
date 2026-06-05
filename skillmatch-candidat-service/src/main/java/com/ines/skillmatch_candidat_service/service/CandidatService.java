package com.ines.skillmatch_candidat_service.service;

import com.ines.skillmatch_candidat_service.dto.CandidatDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.repository.CandidatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // 1. RÉCUPÉRATION PAR ID
    public Candidat getById(Long id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + id));
    }

    // 2. RÉCUPÉRATION PAR USER_ID (Corrigé : Crée et sauvegarde si absent)
    @Transactional
    public Candidat getByUserId(Long userId) {
        return candidatRepository.findByUserId(userId)
                .orElseGet(() -> {
                    System.out.println("⚠️ Aucun candidat trouvé pour userId " + userId + ". Création d'un profil par défaut...");
                    Candidat nouveauCandidat = Candidat.builder()
                            .userId(userId)
                            .nom("Candidat")
                            .prenom("Nouveau")
                            .bio("Complétez votre bio pour attirer les recruteurs.")
                            .validationStatut(Candidat.ValidationStatut.EN_ATTENTE)
                            .build();
                    // 🎯 TRÈS IMPORTANT : On le persiste en BDD pour qu'il existe physiquement !
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

    // 4. MISE À JOUR AVEC CV
    @Transactional
    public Candidat updateProfil(Long userId, String prenom, String nom, String portfolioUrl, MultipartFile cv, MultipartFile photo) throws IOException {

        Candidat c = candidatRepository.findByUserId(userId).orElseThrow();

        c.setPortfolioUrl(portfolioUrl);

        // Sauvegarde de la Photo
        if (photo != null && !photo.isEmpty()) {
            String photoName = "photo_" + userId + ".jpg";
            Path path = Paths.get("uploads/photos/");
            if (!Files.exists(path)) Files.createDirectories(path);
            Files.copy(photo.getInputStream(), path.resolve(photoName), StandardCopyOption.REPLACE_EXISTING);
            c.setPhotoPath(photoName);
        }


        return candidatRepository.save(c);
    }

    // 5. VALIDATION ADMIN (Correction du nom ValidationStatut)
    @Transactional
    public Candidat updateValidationStatus(Long id, Candidat.ValidationStatut statut) {
        Candidat candidat = this.getById(id); // Utilisation de la méthode interne
        candidat.setValidationStatut(statut);
        return candidatRepository.save(candidat);
    }

    // 6. RECHERCHE
    public List<Candidat> searchByCompetence(String competence) {
        return candidatRepository.findByCompetence(competence);
    }

    public List<Candidat> getAll() {
        return candidatRepository.findAll();
    }

    // Utilitaire de sauvegarde de fichier
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path uploadPath = Paths.get(uploadDir, subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    // 4. MISE À JOUR AVEC CV
    @Transactional // Assure le commit SQL en fin de traitement
    public Candidat updateProfilWithFile(Long userId, CandidatDTO dto, MultipartFile cv, MultipartFile photo) throws IOException {

        // 1. On récupère le candidat existant lié à cet utilisateur
        Candidat candidatExistant = candidatRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Candidat introuvable pour le user ID : " + userId));

        // 2. On injecte l'intégralité des valeurs textuelles
        candidatExistant.setPrenom(dto.getPrenom());
        candidatExistant.setNom(dto.getNom());
        candidatExistant.setTelephone(dto.getTelephone());
        candidatExistant.setAdresse(dto.getAdresse());
        candidatExistant.setBio(dto.getBio());
        candidatExistant.setCompetences(dto.getCompetences());
        candidatExistant.setLinkedinUrl(dto.getLinkedinUrl());
        candidatExistant.setPortfolioUrl(dto.getPortfolioUrl());
        candidatExistant.setNiveauScolaire(dto.getNiveauScolaire());

        // 3. Traitement sécurisé du fichier CV
        if (cv != null && !cv.isEmpty()) {
            String cvName = saveFile(cv, "cvs");
            candidatExistant.setCvPath(cvName);
        }

        // 4. Traitement sécurisé de la Photo
        if (photo != null && !photo.isEmpty()) {
            String photoName = saveFile(photo, "photos");
            candidatExistant.setPhotoPath(photoName);
        }

        // 5. Sauvegarde de l'entité mise à jour (Déclenche un SQL UPDATE automatique)
        return candidatRepository.save(candidatExistant);
    }
}