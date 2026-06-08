package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// On lie l'interface à sa classe de secours en cas de panne
@FeignClient(name = "skillmatch-candidat-service", fallback = CandidatClientFallback.class)
public interface CandidatClient {
    @GetMapping("/api/candidats/user/{userId}")
    Map<String, Object> getProfil(@PathVariable("userId") Long userId);
}