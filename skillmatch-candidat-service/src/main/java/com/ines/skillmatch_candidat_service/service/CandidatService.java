package com.ines.skillmatch_candidat_service.service;
import com.ines.skillmatch_candidat_service.dto.CandidatDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.repository.CandidatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
// Import pour @Value
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public Candidat getById(Long id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + id));
    }

    public Candidat getByUserId(Long userId) {
        return candidatRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil candidat non trouvé pour l'utilisateur: " + userId));
    }

    @Transactional
    public Candidat createOrUpdate(Long userId, CandidatDTO dto) {
        Candidat candidat = candidatRepository.findByUserId(userId)
                .orElse(Candidat.builder().userId(userId).build());

        // Mise à jour des champs
        candidat.setNom(dto.getNom());
        candidat.setPrenom(dto.getPrenom());
        candidat.setTelephone(dto.getTelephone());
        candidat.setAdresse(dto.getAdresse());
        candidat.setBio(dto.getBio());
        candidat.setCompetences(dto.getCompetences());
        candidat.setLinkedinUrl(dto.getLinkedinUrl());
        candidat.setPortfolioUrl(dto.getPortfolioUrl());
        candidat.setNiveauScolaire(dto.getNiveauScolaire());

        return candidatRepository.save(candidat);
    }

    @Transactional
    public String uploadCV(Long userId, MultipartFile file) throws IOException {
        Candidat candidat = getByUserId(userId);

        // Créer le dossier uploads s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir, "cv");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Mettre à jour le chemin dans la base
        candidat.setCvPath(filePath.toString());
        candidatRepository.save(candidat);

        return filePath.toString();
    }

    @Transactional
    public String uploadPhoto(Long userId, MultipartFile file) throws IOException {
        Candidat candidat = getByUserId(userId);

        Path uploadPath = Paths.get(uploadDir, "photos");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        candidat.setPhotoPath(filePath.toString());
        candidatRepository.save(candidat);

        return filePath.toString();
    }

    @Transactional
    public Candidat updateValidationStatus(Long id, Candidat.ValidationStatut statut) {
        Candidat candidat = getById(id);
        candidat.setValidationStatut(statut);
        return candidatRepository.save(candidat);
    }

    public List<Candidat> searchByCompetence(String competence) {
        return candidatRepository.findByCompetence(competence);
    }

    public List<Candidat> getAll() {
        return candidatRepository.findAll();
    }


}
