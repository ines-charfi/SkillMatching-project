package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntrepriseClientFallback implements EntrepriseClient {

    @Override
    public Map<String, Object> getByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nomEntreprise", "Entreprise Indisponible");
        fallback.put("description", "Impossible de charger les données de l'entreprise (Le service entreprise est actuellement hors-ligne).");
        fallback.put("secteur", "Non spécifié");
        return fallback;
    }

    @Override
    public Map<String, Object> updateProfil(Long userId, String nomEntreprise, String secteur,
                                            String description, String siteWeb, String telephone,
                                            String contactEmail, MultipartFile logo) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("success", false);
        fallback.put("error", "Le service entreprise ne répond pas. Impossible de sauvegarder les modifications pour le moment.");
        return fallback;
    }

    @Override
    public ResponseEntity<byte[]> getLogo(Long id) {
        // En cas de panne du service, on retourne un code HTTP 503 (Service Unavailable)
        // Cela permettra à ton contrôleur frontend d'intercepter la panne et d'afficher une image/logo par défaut (placeholder)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}