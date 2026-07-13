package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.dto.CandidatureDTO;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.model.Entretien;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import com.ines.skillmatch_candidature_service.repository.EntretienRepository;
import com.ines.skillmatch_candidature_service.service.CandidatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing job applications (Candidatures).
 * Handles application submissions, status updates, and interview scheduling.
 */
@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    // Dependency injection via constructor
    private final CandidatureService candidatureService;
    private final CandidatureRepository candidatureRepository;
    private final EntretienRepository entretienRepository;

    public CandidatureController(CandidatureService candidatureService,
                                 CandidatureRepository candidatureRepository,
                                 EntretienRepository entretienRepository) {
        this.candidatureService = candidatureService;
        this.candidatureRepository = candidatureRepository;
        this.entretienRepository = entretienRepository;
    }

    /**
     * Submit a new job application.
     */
    @PostMapping
    public ResponseEntity<Candidature> postuler(@RequestParam Long candidatId,
                                                @RequestParam Long offreId) {
        return ResponseEntity.ok(candidatureService.postuler(candidatId, offreId));
    }

    /**
     * Retrieve all applications submitted by a specific candidate.
     */
    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<Candidature>> getByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(candidatureService.getByCandidat(candidatId));
    }

    /**
     * Retrieve all applications associated with a specific job offer.
     */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Candidature>> getByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.getByOffre(offreId));
    }

    /**
     * Update the workflow status of an application.
     */
    @PostMapping("/{id}/statut")
    public ResponseEntity<Candidature> updateStatut(@PathVariable Long id,
                                                    @RequestParam String statut) {
        return ResponseEntity.ok(candidatureService.updateStatut(id, statut));
    }

    /**
     * Count the total number of applications for a job offer using Path Variable.
     */
    @GetMapping("/offre/{offreId}/count")
    public ResponseEntity<Long> countByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.countByOffre(offreId));
    }

    /**
     * Count the total number of applications for a job offer using Request Parameter.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countByOffreIdParam(@RequestParam("offreId") Long offreId) {
        return ResponseEntity.ok(candidatureService.countByOffre(offreId));
    }

    /**
     * Count the total number of applications submitted by a candidate.
     */
    @GetMapping("/candidat/{candidatId}/count")
    public ResponseEntity<Long> countByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(candidatureService.countByCandidat(candidatId));
    }

    /**
     * Fetch global recruitment statistics for a specific company.
     */
    @GetMapping("/stats/entreprise/{entrepriseId}")
    public ResponseEntity<Map<String, Object>> statsEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(candidatureService.getStatsEntreprise(entrepriseId));
    }

    /**
     * Retrieve a detailed list of applications for a specific company.
     */
    @GetMapping("/entreprise/{entrepriseId}")
    public List<CandidatureDTO> getByEntreprise(@PathVariable Long entrepriseId) {
        return candidatureService.findAllByEntrepriseId(entrepriseId);
    }

    /**
     * Endpoint to schedule an interview for a selected application.
     * Includes date sanitization and automatic application status transition.
     */
    @PostMapping("/entreprise/entretiens/planifier")
    public ResponseEntity<Void> planifierEntretien(
            @RequestParam("candidatureId") Long candidatureId,
            @RequestParam("date") String dateStr,
            @RequestParam("lieu") String lieu,
            @RequestParam("notes") String notes) {

        try {
            // 1. Verify if the application exists
            Optional<Candidature> candOpt = candidatureRepository.findById(candidatureId);
            if (candOpt.isEmpty()) {
                throw new RuntimeException("Candidature introuvable avec l'ID : " + candidatureId);
            }

            // 🎯 SECURITY FIX FOR DATE: Handle encoding artifacts (e.g., replace '3A' with ':')
            String cleanedDate = dateStr.replaceAll("3A", ":");

            // Build a flexible formatter to handle multiple ISO formats (with or without seconds)
            java.time.format.DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalEnd()
                    .toFormatter();

            LocalDateTime dateConvertie = LocalDateTime.parse(cleanedDate, formatter);

            // 3. Construct and save the Interview entity
            Entretien entretien = Entretien.builder()
                    .candidatureId(candidatureId)
                    .dateEntretien(dateConvertie)
                    .lieu(lieu)
                    .notes(notes)
                    .statut(Entretien.Statut.PROGRAMME)
                    .build();

            entretienRepository.save(entretien);

            // 4. Automatically transition the application status to "ENTRETIEN"
            candidatureService.updateStatut(candidatureId, "ENTRETIEN");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Error handling and logging for debugging purposes
            System.err.println("ERREUR CRASH PLANIFICATION : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur Backend lors de la planification : " + e.getMessage());
        }
    }
}