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

    // 2. RÉCUPÉRATION PAR USER_ID
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

        // On isole uniquement l'extension (.pdf, .jpg, etc.) pour ne pas hériter des anciens UUIDs du nom complet
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Le nom final sera STRICTEMENT composé d'un unique UUID + son extension d'origine
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

        // Remplissage des données textuelles
        candidatExistant.setPrenom(dto.getPrenom());
        candidatExistant.setNom(dto.getNom());
        candidatExistant.setTelephone(dto.getTelephone());
        candidatExistant.setAdresse(dto.getAdresse());
        candidatExistant.setBio(dto.getBio());
        candidatExistant.setCompetences(dto.getCompetences());
        candidatExistant.setLinkedinUrl(dto.getLinkedinUrl());
        candidatExistant.setPortfolioUrl(dto.getPortfolioUrl());
        candidatExistant.setNiveauScolaire(dto.getNiveauScolaire());

        // Sauvegarde du CV s'il y en a un nouveau fourni
        if (cv != null && !cv.isEmpty()) {
            String cvName = saveFile(cv, "cvs");
            candidatExistant.setCvPath(cvName);
        }

        // Sauvegarde de la Photo s'il y en a une nouvelle fournie
        if (photo != null && !photo.isEmpty()) {
            String photoName = saveFile(photo, "photos");
            candidatExistant.setPhotoPath(photoName);
        }

        return candidatRepository.save(candidatExistant);
    }
}