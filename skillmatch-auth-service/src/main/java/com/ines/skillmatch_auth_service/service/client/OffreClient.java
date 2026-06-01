package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "offre-service")
public interface OffreClient {
    @GetMapping("/api/offres/count/all") // Endpoint à créer dans OffreController
    Long countAllOffres();
}