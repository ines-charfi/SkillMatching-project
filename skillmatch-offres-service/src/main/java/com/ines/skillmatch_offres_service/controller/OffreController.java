package com.ines.skillmatch_offres_service.controller;

import com.ines.skillmatch_offres_service.dto.OffreDTO;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.service.OffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;

    // Creates a new job offer (used by companies).
    @PostMapping
    public ResponseEntity<Offre> create(@RequestBody OffreDTO dto) {
        return new ResponseEntity<>(offreService.create(dto), HttpStatus.CREATED);
    }

    // Updates an existing job offer by its ID.
    @PutMapping("/{id}")
    public ResponseEntity<Offre> update(@PathVariable Long id, @RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.update(id, dto));
    }

    // Soft-deletes an offer (logical deletion) by ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offreService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Retrieves a specific offer by its ID (used by the matching service).
    @GetMapping("/{id}")
    public ResponseEntity<Offre> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getById(id));
    }

    // Lists all active offers enriched with company name and application counts.
    @GetMapping
    public ResponseEntity<List<Offre>> getAllActive() {
        return ResponseEntity.ok(offreService.getAllActive());
    }

    // Retrieves all offers belonging to a specific company (used in company dashboard).
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<Offre>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.getByEntreprise(entrepriseId));
    }

    // Searches offers by keyword (title, description, skills) for the frontend search bar.
    @GetMapping("/search")
    public ResponseEntity<List<Offre>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(offreService.search(keyword));
    }

    // Counts the number of offers for a specific company (statistics).
    @GetMapping("/entreprise/{entrepriseId}/count")
    public ResponseEntity<Long> countByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.countByEntreprise(entrepriseId));
    }

    // Returns the total number of all offers (used in the Admin Dashboard).
    @GetMapping("/count")
    public ResponseEntity<Long> countAllOffres() {
        return ResponseEntity.ok(offreService.countAllOffres());
    }

    // Fetches the most recently created offers (for homepage or sidebar).
    @GetMapping("/latest")
    public ResponseEntity<List<Offre>> getLatest() {
        return ResponseEntity.ok(offreService.getLatest());
    }
}