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

    /**
     * AJUSTÉ : Reçoit 'id' et 'statut' en @RequestParam pour matcher le client Feign du Frontend
     */
    @PostMapping("/statut")
    public ResponseEntity<Candidature> updateStatut(@RequestParam("id") Long id,
                                                    @RequestParam("statut") String statut) {
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
    public ResponseEntity<Map<String, Object>> geStatsEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(candidatureService.getStatsEntreprise(entrepriseId));
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public List<CandidatureDTO> getByEntreprise(@PathVariable Long entrepriseId) {
        return candidatureService.findAllByEntrepriseId(entrepriseId);
    }

    @GetMapping("/entretiens/candidature/{candidatureId}")
    public ResponseEntity<List<Entretien>> getEntretiensByCandidature(@PathVariable Long candidatureId) {
        return ResponseEntity.ok(candidatureService.getEntretiensByCandidature(candidatureId));
    }

    /**
     * AJUSTÉ : Mappé sur l'URL attendue par ton Feign Client du Frontend.
     * Enregistre l'entretien en base et passe la candidature en statut 'ENTRETIEN' ou équivalent.
     */
    @PostMapping("/entreprise/entretiens/planifier")
    public ResponseEntity<Void> planifierEntretien(
            @RequestParam("candidatureId") Long candidatureId,
            @RequestParam("date") String dateStr,
            @RequestParam("lieu") String lieu,
            @RequestParam("notes") String notes) {

        try {
            Optional<Candidature> candOpt = candidatureRepository.findById(candidatureId);
            if (candOpt.isEmpty()) {
                throw new RuntimeException("Candidature introuvable avec l'ID : " + candidatureId);
            }

            // Gestion de la conversion de la date provenant du datetime-local HTML
            LocalDateTime dateConvertie = LocalDateTime.parse(dateStr);

            // Construction et sauvegarde de l'entretien
            Entretien entretien = Entretien.builder()
                    .candidatureId(candidatureId)
                    .dateEntretien(dateConvertie)
                    .lieu(lieu)
                    .notes(notes)
                    .statut(Entretien.Statut.PROGRAMME)
                    .build();

            entretienRepository.save(entretien);

            // Met à jour automatiquement la candidature pour indiquer qu'un entretien est fixé
            candidatureService.updateStatut(candidatureId, "ENTRETIEN");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur Backend lors de la planification : " + e.getMessage());
        }
    }
}