package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntrepriseClientFallback implements EntrepriseClient {

    // Fallback for fetching company profile: returns a dummy profile with a placeholder message.
    @Override
    public Map<String, Object> getByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nomEntreprise", "Entreprise Indisponible");
        fallback.put("description", "Impossible de charger les données de l'entreprise (Le service entreprise est actuellement hors-ligne).");
        fallback.put("secteur", "Non spécifié");
        return fallback;
    }

    // Fallback for updating company profile: returns an error indicating service unavailability.
    @Override
    public Map<String, Object> updateProfil(Long userId, String nomEntreprise, String secteur,
                                            String description, String siteWeb, String telephone,
                                            String contactEmail, MultipartFile logo) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("success", false);
        fallback.put("error", "Le service entreprise ne répond pas. Impossible de sauvegarder les modifications pour le moment.");
        return fallback;
    }

    // Fallback for downloading company logo: returns HTTP 503 so the frontend can display a default placeholder.
    @Override
    public ResponseEntity<byte[]> getLogo(Long id) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}