package com.ines.skillmatch_candidat_service.controller;

import com.ines.skillmatch_candidat_service.dto.CandidatDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.service.CandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
/**
 * REST controller for managing candidate (Candidat) profiles.
 * All endpoints are prefixed with "/api/candidats".
 */

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService candidatService;
    /**
     * Initializes a new candidat profile for a given user.
     * This is typically called after user registration to create an empty profile.
     *
     * @param userId the ID of the user for whom the profile is created
     * @param nom    the last name of the candidate
     * @param prenom the first name of the candidate
     */
    @PostMapping("/init")
    public void init(@RequestParam Long userId, @RequestParam String nom, @RequestParam String prenom) {
        candidatService.initCandidat(userId, nom, prenom);
    }
    /**
     * Retrieves the candidat profile associated with a specific user ID.
     *
     * @param userId the user ID
     * @return the Candidat entity if found, otherwise 404
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Candidat> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(candidatService.getByUserId(userId));
    }
    /**
     * Updates the candidate profile for a given user, including optional file uploads (CV and photo).
     * This method accepts multipart/form-data with all fields as @RequestPart to align with Feign clients
     * and avoid "400 Bad Request" errors when sending mixed content.
     */
    //  CORRECTION : Tout passer en @RequestPart pour s'aligner sur Feign Client et éviter le 400 Bad Request
    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidat> updateProfil(
            @PathVariable Long userId,
            @RequestPart("prenom") String prenom,
            @RequestPart("nom") String nom,
            @RequestPart(value = "telephone", required = false) String telephone,
            @RequestPart(value = "adresse", required = false) String adresse,
            @RequestPart(value = "bio", required = false) String bio,
            @RequestPart(value = "competences", required = false) String competences,
            @RequestPart(value = "linkedinUrl", required = false) String linkedinUrl,
            @RequestPart(value = "portfolioUrl", required = false) String portfolioUrl,
            @RequestPart(value = "niveauScolaire", required = false) String niveauScolaire,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
// Logging to verify received data in the backend
        System.out.println("====== BACKEND RECEVOIR DONNEES ======");
        System.out.println("ID Utilisateur ciblé : " + userId);
        System.out.println("Nom & Prénom : " + nom + " " + prenom);
        System.out.println("Photo reçue vide ? " + (photo == null || photo.isEmpty()));
        System.out.println("======================================");
        // Build DTO from received fields
        CandidatDTO dto = CandidatDTO.builder()
                .prenom(prenom)
                .nom(nom)
                .telephone(telephone)
                .adresse(adresse)
                .bio(bio)
                .competences(competences)
                .linkedinUrl(linkedinUrl)
                .portfolioUrl(portfolioUrl)
                .niveauScolaire(niveauScolaire)
                .build();

        return ResponseEntity.ok(candidatService.updateProfilWithFile(userId, dto, cv, photo));
    }
    /**
     * Updates the validation status of a candidate (e.g., PENDING, VALIDATED, REJECTED).
     * This is typically used by an administrator to approve or reject a profile.
     *
     * @param id     the candidate's internal ID (not userId)
     * @param statut the new validation status
     * @return the updated Candidat entity
     */
    @PutMapping("/{id}/validation")
    public ResponseEntity<Candidat> updateValidationStatus(@PathVariable Long id, @RequestParam Candidat.ValidationStatut statut) {
        return ResponseEntity.ok(candidatService.updateValidationStatus(id, statut));
    }
    /**
     * Searches for candidates who possess a specific competence (skill).
     *
     * @param competence the skill to search for (e.g., "Java")
     * @return a list of Candidat entities matching the skill
     */
    @GetMapping("/search")
    public ResponseEntity<List<Candidat>> search(@RequestParam String competence) {
        return ResponseEntity.ok(candidatService.searchByCompetence(competence));
    }
    /**
     * Downloads the CV file of a candidate identified by userId.
     * The file is served as an attachment with proper content type (PDF).
     *
     * @param userId the user ID
     * @return the CV file as a downloadable Resource, or 404 if not found
     */
    @GetMapping("/download/cv/{userId}")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long userId) {
        try {
            Candidat candidat = candidatService.getByUserId(userId);
            if (candidat == null || candidat.getCvPath() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get("uploads", "cvs", candidat.getCvPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println(" Erreur téléchargement CV : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * Serves the profile photo (avatar) of a candidate as a byte array.
     * Automatically detects the content type (image/jpeg, image/png, etc.).
     *
     * @param userId the user ID
     * @return the image bytes with appropriate MediaType, or 404 if not found
     */
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        try {
            Candidat candidat = candidatService.getByUserId(userId);
            if (candidat != null && candidat.getPhotoPath() != null) {
                Path path = Paths.get("uploads", "photos", candidat.getPhotoPath());
                if (Files.exists(path)) {
                    byte[] image = Files.readAllBytes(path);
                    String contentType = Files.probeContentType(path);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                            .body(image);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println(" Erreur affichage avatar : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}