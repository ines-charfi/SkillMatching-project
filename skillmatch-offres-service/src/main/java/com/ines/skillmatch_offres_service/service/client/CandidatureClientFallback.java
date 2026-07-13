package com.ines.skillmatch_offres_service.service.client;

import org.springframework.stereotype.Component;

// Fallback implementation for CandidatureClient – called when the candidature service is unavailable.
@Component
public class CandidatureClientFallback implements CandidatureClient {

    // Fallback for counting applications per offer: returns 0 to avoid blocking the offer display.
    @Override
    public Long CountByOffreId(Long offreId) {
        return 0L;
    }
}