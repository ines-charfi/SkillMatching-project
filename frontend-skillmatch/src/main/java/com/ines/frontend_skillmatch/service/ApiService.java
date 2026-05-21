package com.ines.frontend_skillmatch.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ApiService {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public ApiService(RestTemplate restTemplate,
                      @Value("${api.gateway.url}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    public <T> T get(String path, Class<T> type, String token) {
        ResponseEntity<T> response = restTemplate.exchange(
                gatewayUrl + path, HttpMethod.GET,
                new HttpEntity<>(headers(token)), type
        );
        return response.getBody();
    }

    public <T> T post(String path, Object body, Class<T> type, String token) {
        ResponseEntity<T> response = restTemplate.exchange(
                gatewayUrl + path, HttpMethod.POST,
                new HttpEntity<>(body, headers(token)), type
        );
        return response.getBody();
    }

    public void put(String path, Object body, String token) {
        restTemplate.exchange(
                gatewayUrl + path, HttpMethod.PUT,
                new HttpEntity<>(body, headers(token)), Void.class
        );
    }

    public void delete(String path, String token) {
        restTemplate.exchange(
                gatewayUrl + path, HttpMethod.DELETE,
                new HttpEntity<>(headers(token)), Void.class
        );
    }

    // ============================================
    // AUTH
    // ============================================
    public Map login(String email, String password) {
        return post("/api/auth/login", Map.of("email", email, "password", password), Map.class, null);
    }

    public Map register(Map data) {
        return post("/api/auth/register", data, Map.class, null);
    }

    // ============================================
    // CANDIDAT
    // ============================================
    public Map getProfilCandidat(Long userId, String token) {
        return get("/api/candidats/user/" + userId, Map.class, token);
    }

    public Map updateProfilCandidat(Long userId, Map data, String token) {
        return post("/api/candidats/user/" + userId, data, Map.class, token);
    }

    public List getExperiences(Long userId, String token) {
        return get("/api/candidats/experiences/user/" + userId, List.class, token);
    }

    public Map updateProfilCandidat(Long userId, String prenom, String nom, String bio, String competences, String niveau, org.springframework.web.multipart.MultipartFile cv, String token) {
        String url = gatewayUrl + "/api/candidats/user/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("prenom", prenom);
        body.add("nom", nom);
        body.add("bio", bio);
        body.add("competences", competences);
        body.add("niveauScolaire", niveau);

        // On ajoute le fichier s'il existe
        if (cv != null && !cv.isEmpty()) {
            body.add("cv", cv.getResource());
        }

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, requestEntity, Map.class);
    }
    // Dans ApiService.java
    public void updateProfilCandidatFull(Long userId, String prenom, String nom, String telephone, String ville,
                                         String adresse, String bio, String competences, String niveau,
                                         org.springframework.web.multipart.MultipartFile cv, String token) {
        String url = gatewayUrl + "/api/candidats/user/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("prenom", prenom);
        body.add("nom", nom);
        body.add("telephone", telephone);
        body.add("ville", ville);
        body.add("adresse", adresse);
        body.add("bio", bio);
        body.add("competences", competences);
        body.add("niveauScolaire", niveau);

        if (cv != null && !cv.isEmpty()) {
            body.add("cv", cv.getResource());
        }

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForObject(url, entity, Map.class);
    }
    // ============================================
    // ENTREPRISE
    // ============================================
    public Map getProfilEntreprise(Long userId, String token) {
        return get("/api/entreprises/user/" + userId, Map.class, token);
    }

    public Map updateProfilEntreprise(Long userId, Map data, String token) {
        return post("/api/entreprises/user/" + userId, data, Map.class, token);
    }

    // ============================================
    // OFFRES
    // ============================================
    public List getAllOffres(String token) {
        return get("/api/offres", List.class, token);
    }

    public List getOffresByEntreprise(Long entrepriseId, String token) {
        return get("/api/offres/entreprise/" + entrepriseId, List.class, token);
    }

    public Map createOffre(Map data, String token) {
        return post("/api/offres", data, Map.class, token);
    }

    public void deleteOffre(Long id, String token) {
        delete("/api/offres/" + id, token);
    }

    // ============================================
    // CANDIDATURES
    // ============================================
    public Map postuler(Long candidatId, Long offreId, String token) {
        return post("/api/candidatures?candidatId=" + candidatId + "&offreId=" + offreId,
                null, Map.class, token);
    }

    public List getMesCandidatures(Long candidatId, String token) {
        return get("/api/candidatures/candidat/" + candidatId, List.class, token);
    }

    public List getCandidaturesByOffre(Long offreId, String token) {
        return get("/api/candidatures/offre/" + offreId, List.class, token);
    }

    public void updateStatutCandidature(Long id, String statut, String token) {
        put("/api/candidatures/" + id + "/statut?statut=" + statut, null, token);
    }

    public Map getStatsEntreprise(Long entrepriseId, String token) {
        return get("/api/candidatures/stats/entreprise/" + entrepriseId, Map.class, token);
    }
}