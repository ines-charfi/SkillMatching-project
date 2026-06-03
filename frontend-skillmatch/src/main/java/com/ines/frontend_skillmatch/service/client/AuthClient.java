package com.ines.frontend_skillmatch.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-auth-service", url = "http://auth-service:8081")
public interface AuthClient {

    @PostMapping("/api/auth/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);

    @PostMapping("/api/auth/register")
    Map<String, Object> register(@RequestBody Map<String, Object> registrationData);

    @GetMapping("/api/auth/stats/public")
    Map<String, Object> getPublicStats();

    // --- Routes d'administration Backend ---
    @GetMapping("/api/admin/stats")
    Map<String, Object> getGlobalStats();

    @GetMapping("/api/admin/users")
    List<Map<String, Object>> getAllUsers();

    @PutMapping("/api/admin/users/{id}/toggle")
    void toggleUserStatus(@PathVariable("id") Long id);

    @GetMapping("/api/admin/fichiers-a-verifier")
    List<Map<String, Object>> getFichiersAVerifier();

    @PostMapping("/api/admin/analyser-contenu")
    Map<String, Object> analyserFichierAvecIA(@RequestBody Map<String, String> request);

    // --- NOUVEAU : Communication avec les offres via le service Auth/Admin ---
    @GetMapping("/api/admin/offres")
    List<Map<String, Object>> getAllOffres();

    @DeleteMapping("/api/admin/offres/{id}")
    void supprimerOffre(@PathVariable("id") Long id);

    @GetMapping("/api/admin/fichiers/download-cv/{candidatId}")
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadCv(@PathVariable("candidatId") Long candidatId);

    @GetMapping("/api/admin/fichiers/download-logo/{entrepriseId}")
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadLogo(@PathVariable("entrepriseId") Long entrepriseId);
}