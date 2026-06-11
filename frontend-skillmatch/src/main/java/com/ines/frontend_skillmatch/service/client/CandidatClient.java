package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-candidat-service", configuration = FeignConfig.class, fallback = CandidatClientFallback.class)
public interface CandidatClient {

    // Mise à jour du profil (multipart)
    @PostMapping(value = "/api/candidats/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> updateProfil(
            @PathVariable("userId") Long userId,
            @RequestPart("prenom") String prenom,
            @RequestPart("nom") String nom,
            @RequestPart("telephone") String telephone,
            @RequestPart("adresse") String adresse,
            @RequestPart("bio") String bio,
            @RequestPart("competences") String competences,
            @RequestPart("linkedinUrl") String linkedinUrl,
            @RequestPart("portfolioUrl") String portfolioUrl,
            @RequestPart("niveauScolaire") String niveauScolaire,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    );

    // Récupération du profil (correction du chemin)
    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getProfil(@PathVariable("userId") Long userId);

    // Expériences
    @GetMapping("/api/candidats/experiences/user/{userId}")
    List<Map<String, Object>> getExperiences(@PathVariable("userId") Long userId);

    // Avatar
    @GetMapping("/api/candidats/avatar/{userId}")
    ResponseEntity<byte[]> getAvatar(@PathVariable("userId") Long userId);

    // Téléchargement du CV
    @GetMapping("/api/candidats/download/cv/{userId}")
    ResponseEntity<byte[]> downloadCV(@PathVariable("userId") Long userId);

    // Liste de tous les candidats
    @GetMapping("/api/candidats")
    List<Map<String, Object>> getAllCandidats();

    // Changement de statut (validation)
    @PutMapping("/api/candidats/{id}/statut")
    void validerStatutCandidat(@PathVariable("id") Long id, @RequestParam("statut") String statut);

    // Validation (pour admin)
    @PutMapping("/api/candidats/{id}/validation")
    void updateValidation(@PathVariable("id") Long id, @RequestParam("statut") String statut);
}