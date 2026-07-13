package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Fallback implementation for OffreClient – called when the offer service is unavailable.
@Component
public class OffreClientFallback implements OffreClient {

    // Fallback for fetching all offers: returns an empty list to avoid UI crashes.
    @Override
    public List<Map<String, Object>> getAllOffres() {
        return new ArrayList<>();
    }

    // Fallback for deleting an offer: throws an exception to notify the admin of failure.
    @Override
    public void deleteOffre(Long id) {
        throw new RuntimeException("Impossible de supprimer l'offre. Le service des offres est indisponible.");
    }

    // Fallback for counting offers: returns 0 to avoid breaking the admin dashboard stats.
    @Override
    public Long countAllOffres() {
        return 0L;
    }
}