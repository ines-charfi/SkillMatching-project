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

    // Updates the full candidate profile. Accepts text fields + optional CV & photo files (multipart).
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

    // Fetches the complete profile data for a specific candidate by user ID.
    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getProfil(@PathVariable("userId") Long userId);

    // Retrieves the list of professional experiences for a given candidate.
    @GetMapping("/api/candidats/experiences/user/{userId}")
    List<Map<String, Object>> getExperiences(@PathVariable("userId") Long userId);

    // Downloads the candidate's avatar/profile picture as a byte array.
    @GetMapping("/api/candidats/avatar/{userId}")
    ResponseEntity<byte[]> getAvatar(@PathVariable("userId") Long userId);

    // Downloads the candidate's CV/resume file as a byte array.
    @GetMapping("/api/candidats/download/cv/{userId}")
    ResponseEntity<byte[]> downloadCV(@PathVariable("userId") Long userId);

    // Lists all registered candidates (usually for admin or recruiter dashboards).
    @GetMapping("/api/candidats")
    List<Map<String, Object>> getAllCandidats();

    // Updates the general status of a candidate (e.g., ACTIVE, INACTIVE, SUSPENDED). Admin only.
    @PutMapping("/api/candidats/{id}/statut")
    void validerStatutCandidat(@PathVariable("id") Long id, @RequestParam("statut") String statut);

    // Updates the validation/approval status of a candidate (e.g., APPROVED, REJECTED). Admin only.
    @PutMapping("/api/candidats/{id}/validation")
    void updateValidation(@PathVariable("id") Long id, @RequestParam("statut") String statut);
}