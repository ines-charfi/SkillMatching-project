package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OffreClientFallback implements OffreClient {

    @Override
    public Map<String, Object> getOffre(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("titre", "Offre indisponible temporairement");
        fallback.put("entreprise", "SkillMatch Security Mode");
        fallback.put("statut", "MAINTENANCE");
        return fallback;
    }
    @Override
    public List<Map<String, Object>> getOffresByEntreprise(Long entrepriseId) {
        // En cas de panne du service Offre, on renvoie une liste vide.
        // Cela évite de faire planter l'affichage du profil de l'entreprise sur le frontend.
        return new ArrayList<>();
    }
}
