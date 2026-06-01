package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-candidature-service", configuration = FeignConfig.class)
public interface CandidatureClient {

    @PostMapping("/api/candidatures")
    Map<String, Object> postuler(@RequestParam("candidatId") Long candidatId, @RequestParam("offreId") Long offreId);

    @GetMapping("/api/candidatures/candidat/{userId}")
    List<Map<String, Object>> getByCandidat(@PathVariable("userId") Long userId);

    @GetMapping("/api/candidatures/entreprise/{entrepriseId}")
    List<Map<String, Object>> getByEntreprise(@PathVariable("entrepriseId") Long entrepriseId);

    @GetMapping("/api/candidatures/stats/entreprise/{entrepriseId}")
    Map<String, Object> getStatsEntreprise(@PathVariable("entrepriseId") Long entrepriseId);

    // Synchronisé en POST pour éviter l'erreur 405 Method Not Allowed
    @PostMapping("/api/candidatures/{id}/statut")
    void updateStatut(@PathVariable("id") Long id, @RequestParam("statut") String statut);

    // AJOUTE CETTE MÉTHODE AVEC LES VALUE EXPLICITES POUR LES PARAMS
    @GetMapping(value = "/api/matching/score", consumes = "application/json")
    int getScore(@RequestParam(value = "userId") Long userId, @RequestParam(value = "offreId") Long offreId);
}