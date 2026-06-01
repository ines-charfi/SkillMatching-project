package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.dto.CandidatureDTO;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.service.CandidatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    private final CandidatureService candidatureService;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
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

    // Changé en @PostMapping pour coller au Feign client
    @PostMapping("/{id}/statut")
    public ResponseEntity<Candidature> updateStatut(@PathVariable Long id,
                                                    @RequestParam String statut) {
        return ResponseEntity.ok(candidatureService.updateStatut(id, statut));
    }

    @GetMapping("/offre/{offreId}/count")
    public ResponseEntity<Long> countByOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.countByOffre(offreId));
    }

    // Écoute sur /api/candidatures/count?offreId=... pour satisfaire le client Feign de offre-service
    @GetMapping("/count")
    public ResponseEntity<Long> countByOffreIdParam(@RequestParam("offreId") Long offreId) {
        // On réutilise la méthode existante de ton service qui fonctionne déjà !
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


}