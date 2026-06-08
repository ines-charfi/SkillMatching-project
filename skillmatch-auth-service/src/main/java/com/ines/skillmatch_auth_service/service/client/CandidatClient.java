package com.ines.skillmatch_auth_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "candidat-service", url = "http://candidat-service:8082", fallback = CandidatClientFallback.class)
public interface CandidatClient {
    @PostMapping("/api/candidats/init")
    void initCandidat(@RequestParam("userId") Long userId,
                      @RequestParam("nom") String nom,
                      @RequestParam("prenom") String prenom);

    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getCandidatByUserId(@PathVariable("userId") Long userId);
}