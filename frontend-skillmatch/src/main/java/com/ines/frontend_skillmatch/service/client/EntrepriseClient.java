package com.ines.frontend_skillmatch.service.client;

import com.ines.frontend_skillmatch.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@FeignClient(name = "skillmatch-entreprise-service", configuration = FeignConfig.class, fallback = EntrepriseClientFallback.class)
public interface EntrepriseClient {

    // Fetches the company profile associated with a given user ID.
    @GetMapping("/api/entreprises/user/{userId}")
    Map<String, Object> getByUserId(@PathVariable("userId") Long userId);

    // Updates the company profile. Supports multipart/form-data for optional logo upload.
    @PostMapping(value = "/api/entreprises/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> updateProfil(
            @PathVariable("userId") Long userId,
            @RequestParam("nomEntreprise") String nomEntreprise,
            @RequestParam(value = "secteur", required = false) String secteur,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "siteWeb", required = false) String siteWeb,
            @RequestParam(value = "telephone", required = false) String telephone,
            @RequestParam(value = "contactEmail", required = false) String contactEmail,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    );

    // Retrieves the binary logo image of a company by its ID.
    @GetMapping("/api/entreprises/{id}/logo")
    ResponseEntity<byte[]> getLogo(@PathVariable("id") Long id);
}