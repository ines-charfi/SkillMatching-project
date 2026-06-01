package com.ines.skillmatch_offres_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "skillmatch-candidature-service")
public interface CandidatureClient {
    @GetMapping("/api/candidatures/count")
    Long CountByOffreId(@RequestParam("offreId") Long offreId);
}
