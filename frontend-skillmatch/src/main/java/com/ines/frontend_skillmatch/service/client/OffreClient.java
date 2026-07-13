package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// Feign client for the Offer service. Uses Consul service discovery (name), custom config (timeouts, interceptors), and a fallback for resilience.
@FeignClient(name = "skillmatch-offres-service", configuration = FeignConfig.class, fallback = OffreClientFallback.class)
public interface OffreClient {

    // Retrieves all active job offers.
    @GetMapping("/api/offres")
    List<Map<String, Object>> getAllActive();

    // Retrieves all offers posted by a specific company.
    @GetMapping("/api/offres/entreprise/{entrepriseId}")
    List<Map<String, Object>> getByEntreprise(@PathVariable Long entrepriseId);

    // Creates a new job offer with the provided data.
    @PostMapping("/api/offres")
    Map<String, Object> create(@RequestBody Map<String, Object> offreData);

    // Deletes an offer by its ID.
    @DeleteMapping("/api/offres/{id}")
    void delete(@PathVariable("id") Long id);

    // Fetches a specific offer by its ID (used for loading the edit form).
    @GetMapping("/api/offres/{id}")
    Map<String, Object> getById(@PathVariable("id") Long id);

    // Updates an existing offer with new data (prevents duplicate creation).
    @PutMapping("/api/offres/{id}")
    Map<String, Object> update(@PathVariable("id") Long id, @RequestBody Map<String, Object> offreData);
}