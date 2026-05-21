package com.ines.skillmatch_offres_service.controller;
import com.ines.skillmatch_offres_service.dto.OffreDTO;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.service.OffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;

    @PostMapping
    public ResponseEntity<Offre> create(@RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offre> update(@PathVariable Long id, @RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offreService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offre> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Offre>> getAllActive() {
        return ResponseEntity.ok(offreService.getAllActive());
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<Offre>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.getByEntreprise(entrepriseId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Offre>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(offreService.search(keyword));
    }

    @GetMapping("/entreprise/{entrepriseId}/count")
    public ResponseEntity<Long> countByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.countByEntreprise(entrepriseId));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Offre>> getLatest() {
        return ResponseEntity.ok(offreService.getLatest());
    }
}
