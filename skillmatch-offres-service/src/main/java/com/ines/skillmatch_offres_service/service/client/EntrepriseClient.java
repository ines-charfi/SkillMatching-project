package com.ines.skillmatch_offres_service.service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;


@FeignClient(name = "skillmatch-entreprise-service", fallback = EntrepriseClientFallback.class)
public interface EntrepriseClient {
    @GetMapping("/api/entreprises/user/{userId}")
    Map<String, Object> getEntrepriseByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/api/entreprises/{id}")
    Map<String, Object> getById(@PathVariable("id") Long id);


}
