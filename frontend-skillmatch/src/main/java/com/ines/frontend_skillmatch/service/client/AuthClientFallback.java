package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthClientFallback implements AuthClient {

    @Override
    public Map<String, Object> login(Map<String, String> credentials) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Le service d'authentification est indisponible.");
        fallback.put("authenticated", false);
        return fallback;
    }

    @Override
    public Map<String, Object> register(Map<String, Object> registrationData) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Impossible de créer un compte pour le moment (Service d'inscription hors-ligne).");
        fallback.put("success", false);
        return fallback;
    }

    @Override
    public Map<String, Object> getPublicStats() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("offresCount", 0);
        fallback.put("candidatsCount", 0);
        fallback.put("message", "Statistiques indisponibles");
        return fallback;
    }

    // --- Fallbacks des Routes d'administration Backend ---
    @Override
    public Map<String, Object> getGlobalStats() {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getAllUsers() {
        return new ArrayList<>();
    }

    @Override
    public void toggleUserStatus(Long id) {
        // Mode secours : On lève une exception pour avertir l'administrateur que l'action a échoué
        throw new RuntimeException("Impossible de modifier le statut de l'utilisateur. Le service de sécurité ne répond pas.");
    }

    @Override
    public List<Map<String, Object>> getFichiersAVerifier() {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> analyserFichierAvecIA(Map<String, String> request) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("erreur", "L'analyse IA est momentanément indisponible.");
        return fallback;
    }

    // --- Fallbacks Communication avec les offres via Admin ---
    @Override
    public List<Map<String, Object>> getAllOffres() {
        return new ArrayList<>();
    }

    @Override
    public void supprimerOffre(Long id) {
        throw new RuntimeException("Impossible de supprimer l'offre. Le service Admin est indisponible.");
    }

    @Override
    public ResponseEntity<Resource> downloadCv(Long candidatId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public ResponseEntity<Resource> downloadLogo(Long entrepriseId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}