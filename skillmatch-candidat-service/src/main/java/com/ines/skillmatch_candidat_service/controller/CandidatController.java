package com.ines.skillmatch_candidat_service.controller;
import com.ines.skillmatch_candidat_service.dto.CandidatDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.service.CandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService candidatService;

    @GetMapping("/{id}")
    public ResponseEntity<Candidat> getById(@PathVariable Long id) {
        return ResponseEntity.ok(candidatService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Candidat> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(candidatService.getByUserId(userId));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Candidat> createOrUpdate(
            @PathVariable Long userId,
            @RequestBody CandidatDTO dto) {
        return ResponseEntity.ok(candidatService.createOrUpdate(userId, dto));
    }

    @PostMapping("/user/{userId}/cv")
    public ResponseEntity<String> uploadCV(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String path = candidatService.uploadCV(userId, file);
        return ResponseEntity.ok("CV uploadé avec succès: " + path);
    }

    @PostMapping("/user/{userId}/photo")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String path = candidatService.uploadPhoto(userId, file);
        return ResponseEntity.ok("Photo uploadée avec succès: " + path);
    }

    @PutMapping("/{id}/validation")
    public ResponseEntity<Candidat> updateValidationStatus(
            @PathVariable Long id,
            @RequestParam Candidat.ValidationStatut statut) {
        return ResponseEntity.ok(candidatService.updateValidationStatus(id, statut));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Candidat>> searchByCompetence(@RequestParam String competence) {
        return ResponseEntity.ok(candidatService.searchByCompetence(competence));
    }

    @GetMapping
    public ResponseEntity<List<Candidat>> getAll() {
        return ResponseEntity.ok(candidatService.getAll());
    }
}
