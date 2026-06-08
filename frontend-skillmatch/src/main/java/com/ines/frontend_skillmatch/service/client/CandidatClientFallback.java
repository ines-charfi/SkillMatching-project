package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CandidatClientFallback implements CandidatClient {

    @Override
    public Map<String, Object> updateProfil(Long userId, String prenom, String nom, String telephone,
                                            String adresse, String bio, String competences, String linkedinUrl,
                                            String portfolioUrl, String niveauScolaire, MultipartFile cv, MultipartFile photo) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("success", false);
        fallback.put("error", "Le service candidat est indisponible. Impossible de mettre à jour le profil pour le moment.");
        return fallback;
    }

    @Override
    public Map<String, Object> getProfil(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nom", "Profil");
        fallback.put("prenom", "Indisponible");
        fallback.put("bio", "Impossible de charger les données du profil (Service hors-ligne).");
        return fallback;
    }

    @Override
    public List<Map<String, Object>> getExperiences(Long userId) {
        return new ArrayList<>(); // Renvoie une liste vide pour éviter un plantage de l'affichage des expériences
    }

    @Override
    public ResponseEntity<byte[]> getAvatar(Long userId) {
        // Renvoie un statut 503 Service Unavailable pour que le front sache qu'il faut utiliser un avatar par défaut
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public ResponseEntity<byte[]> downloadCV(Long userId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public List<Map<String, Object>> getAllCandidats() {
        return new ArrayList<>();
    }

    @Override
    public void validerStatutCandidat(Long id, String statut) {
        throw new RuntimeException("Le service candidat ne répond pas. Impossible de modifier le statut.");
    }

    @Override
    public void updateValidation(Long id, String statut) {
        throw new RuntimeException("Le service candidat ne répond pas. Impossible de mettre à jour la validation.");
    }
}