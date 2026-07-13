package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

// Feign client for the Offre microservice. Uses a fixed URL (dev config) and a fallback for resilience.
@FeignClient(name = "skillmatch-offres-service", url = "http://offre-service:8084", fallback = OffreClientFallback.class)
public interface OffreClient {

    // Retrieves all job offers from the offer service.
    @GetMapping("/api/offres")
    List<Map<String, Object>> getAllOffres();

    // Deletes a specific job offer by its ID.
    @DeleteMapping("/api/offres/{id}")
    void deleteOffre(@PathVariable("id") Long id);

    // Counts the total number of job offers (used for statistics).
    @GetMapping("/api/offres/count")
    Long countAllOffres();
}