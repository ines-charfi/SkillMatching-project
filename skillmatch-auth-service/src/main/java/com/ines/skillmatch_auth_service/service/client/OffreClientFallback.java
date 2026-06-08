package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OffreClientFallback implements OffreClient {

    @Override
    public List<Map<String, Object>> getAllOffres() {
        // En cas de panne, on renvoie une liste vide pour ne pas faire planter l'affichage
        return new ArrayList<>();
    }

    @Override
    public void deleteOffre(Long id) {
        // Si un admin essaie de supprimer une offre mais que le service offre est en panne,
        // on jette une exception pour avertir que l'action a échoué.
        throw new RuntimeException("Impossible de supprimer l'offre. Le service des offres est indisponible.");
    }

    @Override
    public Long countAllOffres() {
        // Si le service est hors-ligne, on renvoie 0 pour le compteur du tableau de bord Admin
        return 0L;
    }
}