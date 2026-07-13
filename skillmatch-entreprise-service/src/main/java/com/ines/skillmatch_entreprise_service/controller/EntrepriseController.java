package com.ines.skillmatch_entreprise_service.controller;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.service.EntrepriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/entreprises")
@RequiredArgsConstructor
public class EntrepriseController {

    private final EntrepriseService entrepriseService;
    // Directory where uploaded company logos are stored (configurable via application properties).
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Called by Auth-Service during registration to initialize a company profile.
    @PostMapping("/init")
    public void init(@RequestParam Long userId, @RequestParam String nom) {
        entrepriseService.initEntreprise(userId, nom);
    }

    // Fetches a company profile by the associated user ID.
    @GetMapping("/user/{userId}")
    public ResponseEntity<Entreprise> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(entrepriseService.getByUserId(userId));
    }

    // Updates the company profile from the Enterprise Dashboard. Accepts multipart/form-data for optional logo upload.
    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Entreprise> update(
            @PathVariable Long userId,
            @RequestParam String nomEntreprise,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String siteWeb,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(value = "logo", required = false) MultipartFile logo) throws Exception {

        EntrepriseDTO dto = new EntrepriseDTO();
        dto.setNomEntreprise(nomEntreprise);
        dto.setSecteur(secteur);
        dto.setDescription(description);
        dto.setSiteWeb(siteWeb);
        dto.setTelephone(telephone);
        dto.setContactEmail(contactEmail);

        return ResponseEntity.ok(entrepriseService.updateProfil(userId, dto, logo));
    }

    // Retrieves and streams the company logo image as a byte array.
    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getLogo(@PathVariable Long id) {
        try {
            Entreprise entreprise = entrepriseService.getById(id);
            if (entreprise != null && entreprise.getLogoPath() != null) {
                Path path = Paths.get(uploadDir, "logos", entreprise.getLogoPath());
                if (Files.exists(path)) {
                    byte[] image = Files.readAllBytes(path);
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(image);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Fetches a company by its internal ID (for admin or internal use).
    @GetMapping("/{id}")
    public ResponseEntity<Entreprise> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepriseService.findById(id));
    }
}