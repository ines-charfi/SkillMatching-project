package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "entreprise-service", url = "http://entreprise-service:8083")
public interface EntrepriseClient {
    @PostMapping("/api/entreprises/init")
    void initEntreprise(@RequestParam("userId") Long userId,
                        @RequestParam("nom") String nom);

    @GetMapping("/api/entreprises/user/{userId}")
    Map<String, Object> getEntrepriseByUserId(@PathVariable("userId") Long userId);
}