package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-candidature-service", configuration = FeignConfig.class, fallback = CandidatureClientFallback.class)
public interface CandidatureClient {

    // Submits a new job application for a candidate on a specific offer.
    @PostMapping("/api/candidatures")
    Map<String, Object> postuler(@RequestParam("candidatId") Long candidatId, @RequestParam("offreId") Long offreId);

    // Retrieves all applications submitted by a given candidate.
    @GetMapping("/api/candidatures/candidat/{userId}")
    List<Map<String, Object>> getByCandidat(@PathVariable("userId") Long userId);

    // Retrieves all applications received for a specific company (by its ID).
    @GetMapping("/api/candidatures/entreprise/{entrepriseId}")
    List<Map<String, Object>> getByEntreprise(@PathVariable("entrepriseId") Long entrepriseId);

    // Fetches statistics about applications for a company (e.g., counts by status).
    @GetMapping("/api/candidatures/stats/entreprise/{entrepriseId}")
    Map<String, Object> getStatsEntreprise(@PathVariable("entrepriseId") Long entrepriseId);

    // Updates the status of an application. Uses POST to avoid 405 Method Not Allowed.
    @PostMapping("/api/candidatures/{id}/statut")
    void updateStatut(@PathVariable("id") Long id, @RequestParam("statut") String statut);

    // Computes and returns a matching score between a candidate and a job offer.
    @GetMapping(value = "/api/matching/score", consumes = "application/json")
    int getScore(@RequestParam(value = "userId") Long userId, @RequestParam(value = "offreId") Long offreId);

    // Schedules an interview for a given application with date, location, and notes.
    @PostMapping("/api/candidatures/entreprise/entretiens/planifier")
    void planifierEntretien(
            @RequestParam("candidatureId") Long candidatureId,
            @RequestParam("date") String dateStr,
            @RequestParam("lieu") String lieu,
            @RequestParam("notes") String notes);
}