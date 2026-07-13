package com.ines.frontend_skillmatch.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Feign client for communicating with the 'skillmatch-auth-service'.
 * Handles both public authentication flows and private administrative operations.
 */
@FeignClient(name = "skillmatch-auth-service", url = "http://auth-service:8081", fallback = AuthClientFallback.class)
public interface AuthClient {

    /**
     * ENDPOINT: POST /api/auth/login
     * FUNCTION: Authenticates a user.
     * RETURNS: A map containing the JWT token, role, and user identification details.
     */
    @PostMapping("/api/auth/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);

    /**
     * ENDPOINT: POST /api/auth/register
     * FUNCTION: Registers a new user (Candidate or Recruiter) into the system.
     */
    @PostMapping("/api/auth/register")
    Map<String, Object> register(@RequestBody Map<String, Object> registrationData);

    /**
     * ENDPOINT: GET /api/auth/stats/public
     * FUNCTION: Retrieves platform stats accessible to non-authenticated visitors (total users, active offers).
     */
    @GetMapping("/api/auth/stats/public")
    Map<String, Object> getPublicStats();

    // --- Backend Administration Routes ---

    /**
     * ENDPOINT: GET /api/admin/stats
     * FUNCTION: Retrieves comprehensive platform statistics for the administrator dashboard.
     */
    @GetMapping("/api/admin/stats")
    Map<String, Object> getGlobalStats();

    /**
     * ENDPOINT: GET /api/admin/users
     * FUNCTION: Fetches the list of all users registered on the platform for moderation.
     */
    @GetMapping("/api/admin/users")
    List<Map<String, Object>> getAllUsers();

    /**
     * ENDPOINT: PUT /api/admin/users/{id}/toggle
     * FUNCTION: Switches a user's account status (Enables/Disables account access).
     */
    @PutMapping("/api/admin/users/{id}/toggle")
    void toggleUserStatus(@PathVariable("id") Long id);

    /**
     * ENDPOINT: GET /api/admin/fichiers-a-verifier
     * FUNCTION: Lists all profile files (CVs, logos) currently awaiting administrative approval.
     */
    @GetMapping("/api/admin/fichiers-a-verifier")
    List<Map<String, Object>> getFichiersAVerifier();

    /**
     * ENDPOINT: POST /api/admin/analyser-contenu
     * FUNCTION: Invokes the AI processing service to automatically scan and analyze document content.
     */
    @PostMapping("/api/admin/analyser-contenu")
    Map<String, Object> analyserFichierAvecIA(@RequestBody Map<String, String> request);

    // --- Job Offer Administration (via Auth/Admin service) ---

    /**
     * ENDPOINT: GET /api/admin/offres
     * FUNCTION: Retrieves all job offers across the platform for administrative oversight.
     */
    @GetMapping("/api/admin/offres")
    List<Map<String, Object>> getAllOffres();

    /**
     * ENDPOINT: DELETE /api/admin/offres/{id}
     * FUNCTION: Allows an administrator to remove a specific job offer from the platform.
     */
    @DeleteMapping("/api/admin/offres/{id}")
    void supprimerOffre(@PathVariable("id") Long id);

    /**
     * ENDPOINT: GET /api/admin/fichiers/download-cv/{candidatId}
     * FUNCTION: Fetches the candidate's CV as a downloadable resource for administrative review.
     */
    @GetMapping("/api/admin/fichiers/download-cv/{candidatId}")
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadCv(@PathVariable("candidatId") Long candidatId);

    /**
     * ENDPOINT: GET /api/admin/fichiers/download-logo/{entrepriseId}
     * FUNCTION: Fetches the company's logo as a downloadable resource for administrative review.
     */
    @GetMapping("/api/admin/fichiers/download-logo/{entrepriseId}")
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadLogo(@PathVariable("entrepriseId") Long entrepriseId);
}