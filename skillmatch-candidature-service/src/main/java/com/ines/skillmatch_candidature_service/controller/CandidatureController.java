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

@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    // === DECLARATIONS APPARETANT CORRECTEMENT POUR ÉVITER LE CANNOT RESOLVE ===
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

    @PostMapping
    public ResponseEntity<Candidature> postuler(@RequestParam Long candidatId,
                                                @RequestParam Long offreId) {
        return ResponseEntity.ok(candidatureService.postuler(candidatId, offreId));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<Candidature>> getByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(candidatureService.getByCandidat(candidatId));
    }

    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Candidature>> getByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.getByOffre(offreId));
    }

    @PostMapping("/{id}/statut")
    public ResponseEntity<Candidature> updateStatut(@PathVariable Long id,
                                                    @RequestParam String statut) {
        return ResponseEntity.ok(candidatureService.updateStatut(id, statut));
    }

    @GetMapping("/offre/{offreId}/count")
    public ResponseEntity<Long> countByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.countByOffre(offreId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countByOffreIdParam(@RequestParam("offreId") Long offreId) {
        return ResponseEntity.ok(candidatureService.countByOffre(offreId));
    }

    @GetMapping("/candidat/{candidatId}/count")
    public ResponseEntity<Long> countByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(candidatureService.countByCandidat(candidatId));
    }

    @GetMapping("/stats/entreprise/{entrepriseId}")
    public ResponseEntity<Map<String, Object>> statsEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(candidatureService.getStatsEntreprise(entrepriseId));
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public List<CandidatureDTO> getByEntreprise(@PathVariable Long entrepriseId) {
        return candidatureService.findAllByEntrepriseId(entrepriseId);
    }

    @PostMapping("/entreprise/entretiens/planifier")
    public ResponseEntity<Void> planifierEntretien(
            @RequestParam("candidatureId") Long candidatureId,
            @RequestParam("date") String dateStr,
            @RequestParam("lieu") String lieu,
            @RequestParam("notes") String notes) {

        try {
            // 1. Vérification de la candidature
            Optional<Candidature> candOpt = candidatureRepository.findById(candidatureId);
            if (candOpt.isEmpty()) {
                throw new RuntimeException("Candidature introuvable avec l'ID : " + candidatureId);
            }

            // 🎯 FIX SÉCURISÉ POUR LA DATE : Nettoyage et formateur flexible
            // Remplace les résidus d'encodage comme '3A' ou les caractères corrompus par des ':'
            String cleanedDate = dateStr.replaceAll("3A", ":");

            // Formateur capable de lire les formats avec ou sans secondes (ex: 2026-06-12T11:15 ou 2026-06-12T11:15:00)
            java.time.format.DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalEnd()
                    .toFormatter();

            LocalDateTime dateConvertie = LocalDateTime.parse(cleanedDate, formatter);

            // 3. Construction et sauvegarde de l'entretien
            Entretien entretien = Entretien.builder()
                    .candidatureId(candidatureId)
                    .dateEntretien(dateConvertie)
                    .lieu(lieu)
                    .notes(notes)
                    .statut(Entretien.Statut.PROGRAMME)
                    .build();

            entretienRepository.save(entretien);

            // 4. Mutation automatique du statut de la candidature
            candidatureService.updateStatut(candidatureId, "ENTRETIEN");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("ERREUR CRASH PLANIFICATION : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur Backend lors de la planification : " + e.getMessage());
        }
    }
}