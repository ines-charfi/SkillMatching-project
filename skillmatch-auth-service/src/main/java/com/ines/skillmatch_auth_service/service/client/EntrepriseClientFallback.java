package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntrepriseClientFallback implements EntrepriseClient {

    @Override
    public void initEntreprise(Long userId, String nom) {
        // Mode secours : Si le service entreprise est en panne au moment où un recruteur crée son compte,
        // on jette une exception pour stopper l'inscription proprement plutôt que de créer un compte incomplet.
        throw new RuntimeException("Impossible d'initialiser le compte entreprise. Le service entreprise est indisponible.");
    }

    @Override
    public Map<String, Object> getEntrepriseByUserId(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("nomEntreprise", "Indisponible");
        fallback.put("erreur", "Le service entreprise est actuellement hors-ligne.");
        return fallback;
    }
}