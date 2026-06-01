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

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService candidatService;

    @PostMapping("/init")
    public void init(@RequestParam Long userId, @RequestParam String nom, @RequestParam String prenom) {
        candidatService.initCandidat(userId, nom, prenom);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Candidat> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(candidatService.getByUserId(userId));
    }

    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidat> updateProfil(
            @PathVariable Long userId,
            @RequestParam("prenom") String prenom,
            @RequestParam("nom") String nom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String competences,
            @RequestParam(required = false) String linkedinUrl,
            @RequestParam(required = false) String portfolioUrl,
            @RequestParam(required = false) String niveauScolaire,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws IOException {

        System.out.println("====== BACKEND RECEVOIR DONNEES ======");
        System.out.println("ID Utilisateur ciblé : " + userId);
        System.out.println("Nom & Prénom : " + nom + " " + prenom);
        System.out.println("Photo reçue vide ? " + (photo == null || photo.isEmpty()));
        System.out.println("======================================");

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

    @PutMapping("/{id}/validation")
    public ResponseEntity<Candidat> updateValidationStatus(@PathVariable Long id, @RequestParam Candidat.ValidationStatut statut) {
        return ResponseEntity.ok(candidatService.updateValidationStatus(id, statut));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Candidat>> search(@RequestParam String competence) {
        return ResponseEntity.ok(candidatService.searchByCompetence(competence));
    }

    // ============================================
    // ENDPOINT : TÉLÉCHARGER LE CV
    // ============================================
    @GetMapping("/download/cv/{userId}")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long userId) {
        try {
            Candidat candidat = candidatService.getByUserId(userId);

            if (candidat == null || candidat.getCvPath() == null) {
                return ResponseEntity.notFound().build();
            }

            // 💡 CORRECTION : Pointer vers le bon sous-dossier "uploads/cvs"
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
            System.err.println("❌ Erreur téléchargement CV : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================
    // ENDPOINT : AFFICHER LA PHOTO DE PROFIL
    // ============================================
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        try {
            Candidat candidat = candidatService.getByUserId(userId);

            if (candidat != null && candidat.getPhotoPath() != null) {
                // 💡 CORRECTION : Pointer vers le bon sous-dossier "uploads/photos"
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
            System.err.println("❌ Erreur affichage avatar : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}