package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

// CORRECTION : Changement de 'name' pour matcher Consul et suppression de l'URL en dur
@FeignClient(name = "skillmatch-candidat-service", configuration = FeignConfig.class)
public interface CandidatClient {

    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getProfil(@PathVariable("userId") Long userId);

    @PostMapping(value = "/api/candidats/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> updateProfil(
            @PathVariable("userId") Long userId,
            @RequestParam("prenom") String prenom,
            @RequestParam("nom") String nom,
            @RequestParam(value = "telephone", required = false) String telephone,
            @RequestParam(value = "adresse", required = false) String adresse,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "competences", required = false) String competences,
            @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestParam(value = "portfolioUrl", required = false) String portfolioUrl,
            @RequestParam(value = "niveauScolaire", required = false) String niveauScolaire,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    );

    @GetMapping("/api/candidats/experiences/user/{userId}")
    List<Map<String, Object>> getExperiences(@PathVariable("userId") Long userId);

    @PutMapping("/api/candidats/{id}/validation")
    void updateValidation(@PathVariable("id") Long id, @RequestParam("statut") String statut);

    // Pour récupérer les octets de la photo de profil
    @GetMapping("/api/candidats/avatar/{userId}")
    org.springframework.http.ResponseEntity<byte[]> getAvatar(@PathVariable("userId") Long userId);

    // Pour récupérer le flux du fichier CV
    @GetMapping("/api/candidats/download/cv/{userId}")
    ResponseEntity<byte[]> downloadCV(@PathVariable("userId") Long userId);

}
