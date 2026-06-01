package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

// On change le name ici pour correspondre exactement au nom dans Consul
@FeignClient(name = "skillmatch-offres-service")
public interface OffreClient {

    @GetMapping("/api/offres/{id}")
    Map<String, Object> getOffre(@PathVariable("id") Long id);

    @GetMapping("/api/offres/entreprise/{entrepriseId}")
    List<Map<String, Object>> getOffresByEntreprise(@PathVariable("entrepriseId") Long entrepriseId);
}