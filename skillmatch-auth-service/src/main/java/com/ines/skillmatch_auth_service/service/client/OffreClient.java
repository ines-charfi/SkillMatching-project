package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

// Ton annotation @FeignClient doit déjà être là, ne touche qu'aux méthodes en dessous :
@FeignClient(name = "skillmatch-offre-service", url = "http://offre-service:8084") // ou ton URL actuelle
public interface OffreClient {

    // Garde tes méthodes actuelles (comme countAllOffres()) et AJOUTE ces deux-là :

    @GetMapping("/api/offres") // Vérifie que c'est bien le bon endpoint de ton microservice Offre
    List<Map<String, Object>> getAllOffres();

    @DeleteMapping("/api/offres/{id}") // Vérifie aussi cet endpoint de suppression
    void deleteOffre(@PathVariable("id") Long id);

    @GetMapping("/api/offres/count") // Exemple de ce que tu avais peut-être déjà
    Long countAllOffres();
}