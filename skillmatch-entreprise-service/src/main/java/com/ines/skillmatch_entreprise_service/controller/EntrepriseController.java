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
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Appelé par Auth-Service lors du register
    @PostMapping("/init")
    public void init(@RequestParam Long userId, @RequestParam String nom) {
        entrepriseService.initEntreprise(userId, nom);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Entreprise> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(entrepriseService.getByUserId(userId));
    }

    // Mise à jour complète via le Dashboard Entreprise
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

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getLogo(@PathVariable Long id) {
        try {
            Entreprise entreprise = entrepriseService.getById(id);
            if (entreprise != null && entreprise.getLogoPath() != null) {
                // Construit le chemin vers le fichier (ex: uploads/logos/nom_du_fichier.png)
                Path path = Paths.get(uploadDir, "logos", entreprise.getLogoPath());
                if (Files.exists(path)) {
                    byte[] image = Files.readAllBytes(path);
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG) // Accepte PNG et JPG sur les navigateurs modernes
                            .body(image);
                }
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Entreprise> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepriseService.findById(id));
    }
}