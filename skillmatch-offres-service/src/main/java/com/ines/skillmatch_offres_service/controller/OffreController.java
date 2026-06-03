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

    // 1. CRÉATION (Utilisé par l'Entreprise)
    @PostMapping
    public ResponseEntity<Offre> create(@RequestBody OffreDTO dto) {
        return new ResponseEntity<>(offreService.create(dto), HttpStatus.CREATED);
    }

    // 2. MISE À JOUR
    @PutMapping("/{id}")
    public ResponseEntity<Offre> update(@PathVariable Long id, @RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.update(id, dto));
    }

    // 3. SUPPRESSION (Désactivation logique)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offreService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 4. RÉCUPÉRATION PAR ID (Crucial pour le Matching Service)
    @GetMapping("/{id}")
    public ResponseEntity<Offre> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getById(id));
    }

    // 5. LISTE TOUTES LES OFFRES ACTIVES (Utilisé par le Dashboard Candidat)
    @GetMapping
    public ResponseEntity<List<Offre>> getAllActive() {
        // Cette méthode renvoie maintenant des offres enrichies (Nom entreprise, Nombre candidatures)
        return ResponseEntity.ok(offreService.getAllActive());
    }

    // 6. OFFRES D'UNE ENTREPRISE SPÉCIFIQUE (Maquette 4)
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<Offre>> getByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.getByEntreprise(entrepriseId));
    }

    // 7. RECHERCHE PAR MOT-CLÉ (Utilisé par ta barre de recherche JS)
    @GetMapping("/search")
    public ResponseEntity<List<Offre>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(offreService.search(keyword));
    }

    // 8. STATISTIQUES (Nombre d'offres pour une entreprise)
    @GetMapping("/entreprise/{entrepriseId}/count")
    public ResponseEntity<Long> countByEntreprise(@PathVariable Long entrepriseId) {
        return ResponseEntity.ok(offreService.countByEntreprise(entrepriseId));
    }


    // 10. COMPTAGE GLOBAL (Ajouté pour le Dashboard Admin)
    @GetMapping("/count")
    public ResponseEntity<Long> countAllOffres() {
        // Appelle la méthode de ton service qui compte toutes les offres.
        // Si elle n'existe pas, tu peux utiliser ton repository (ex: offreRepository.count())
        return ResponseEntity.ok(offreService.countAllOffres());
    }
    // 9. DERNIÈRES OFFRES (Pour la page d'accueil ou sidebar)
    @GetMapping("/latest")
    public ResponseEntity<List<Offre>> getLatest() {
        return ResponseEntity.ok(offreService.getLatest());
    }

}