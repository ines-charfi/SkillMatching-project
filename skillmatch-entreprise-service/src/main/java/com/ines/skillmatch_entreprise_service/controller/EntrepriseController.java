package com.ines.skillmatch_entreprise_service.controller;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.service.EntrepriseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/entreprises")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entreprise> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepriseService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Entreprise> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(entrepriseService.getByUserId(userId));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Entreprise> createOrUpdate(@PathVariable Long userId,
                                                     @RequestBody EntrepriseDTO dto) {
        return ResponseEntity.ok(entrepriseService.createOrUpdate(userId, dto));
    }

    @PostMapping("/user/{userId}/logo")
    public ResponseEntity<String> uploadLogo(@PathVariable Long userId,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        String path = entrepriseService.uploadLogo(userId, file);
        return ResponseEntity.ok("Logo uploadé avec succès: " + path);
    }
}