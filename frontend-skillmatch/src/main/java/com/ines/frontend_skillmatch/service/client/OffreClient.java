package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-offres-service", configuration = FeignConfig.class)
public interface OffreClient {

    @GetMapping("/api/offres")
    List<Map<String, Object>> getAllActive();

    @GetMapping("/api/offres/entreprise/{entrepriseId}")
    List<Map<String, Object>> getByEntreprise(@PathVariable Long entrepriseId);

    @PostMapping("/api/offres")
    Map<String, Object> create(@RequestBody Map<String, Object> offreData);

    @DeleteMapping("/api/offres/{id}")
    void delete(@PathVariable("id") Long id);

    //  FIX 1 : Récupérer une offre par son ID (Requis pour charger le formulaire de modification)
    @GetMapping("/api/offres/{id}")
    Map<String, Object> getById(@PathVariable("id") Long id);

    //  FIX 2 : Mettre à jour l'offre existante (Requis pour soumettre la modification sans créer de doublon)
    @PutMapping("/api/offres/{id}")
    Map<String, Object> update(@PathVariable("id") Long id, @RequestBody Map<String, Object> offreData);

}