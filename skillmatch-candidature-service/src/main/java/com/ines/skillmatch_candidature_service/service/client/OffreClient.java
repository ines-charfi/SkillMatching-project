package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * Feign client for interacting with the 'skillmatch-offres-service'.
 * The name must match the service registration name in Consul.
 * Includes a fallback mechanism for fault tolerance.
 */
@FeignClient(name = "skillmatch-offres-service", fallback = OffreClientFallback.class)
public interface OffreClient {

    /**
     * Retrieves specific offer details by its unique identifier.
     *
     * @param id The ID of the offer to retrieve.
     * @return A Map containing the offer data.
     */
    @GetMapping("/api/offres/{id}")
    Map<String, Object> getOffre(@PathVariable("id") Long id);

    /**
     * Retrieves all job offers associated with a specific company.
     *
     * @param entrepriseId The unique identifier of the company.
     * @return A list of job offers as Maps.
     */
    @GetMapping("/api/offres/entreprise/{entrepriseId}")
    List<Map<String, Object>> getOffresByEntreprise(@PathVariable("entrepriseId") Long entrepriseId);
}