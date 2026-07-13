package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;

// Fallback implementation for CandidatureClient – called when the candidature service is unavailable.
@Component
public class CandidatureClientFallback implements CandidatureClient {

    // Fallback for counting applications: returns 0 to avoid breaking the admin dashboard stats.
    @Override
    public Long countAllCandidatures() {
        return 0L;
    }
}