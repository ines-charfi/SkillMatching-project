package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback implementation of {@link AuthClient} used when the actual authentication/administration
 * service is unavailable (e.g., circuit breaker open, network failure, timeout).
 * Provides sensible default responses or throws runtime exceptions to signal the failure.
 */
@Component
public class AuthClientFallback implements AuthClient {

    /**
     * Fallback for the login endpoint.
     * Returns a map indicating that authentication is unavailable.
     *
     * @param credentials the login credentials (ignored in fallback)
     * @return a map with an error message and authenticated = false
     */
    @Override
    public Map<String, Object> login(Map<String, String> credentials) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Le service d'authentification est indisponible.");
        fallback.put("authenticated", false);
        return fallback;
    }

    /**
     * Fallback for the registration endpoint.
     * Returns a map indicating that account creation is not possible at the moment.
     *
     * @param registrationData the registration data (ignored in fallback)
     * @return a map with an error message and success = false
     */
    @Override
    public Map<String, Object> register(Map<String, Object> registrationData) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Impossible de créer un compte pour le moment (Service d'inscription hors-ligne).");
        fallback.put("success", false);
        return fallback;
    }

    /**
     * Fallback for retrieving public statistics.
     * Returns zeroed statistics with a message indicating unavailability.
     *
     * @return a map with zero counts and a message
     */
    @Override
    public Map<String, Object> getPublicStats() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("offresCount", 0);
        fallback.put("candidatsCount", 0);
        fallback.put("message", "Statistiques indisponibles");
        return fallback;
    }

    // --- Fallbacks for Backend Administration Routes ---

    /**
     * Fallback for global statistics (admin only).
     * Returns an empty map because no data is available.
     *
     * @return an empty HashMap
     */
    @Override
    public Map<String, Object> getGlobalStats() {
        return new HashMap<>();
    }

    /**
     * Fallback for retrieving all users (admin only).
     * Returns an empty list.
     *
     * @return an empty ArrayList
     */
    @Override
    public List<Map<String, Object>> getAllUsers() {
        return new ArrayList<>();
    }

    /**
     * Fallback for toggling a user's active status (admin only).
     * Throws a runtime exception to notify the administrator that the action failed.
     *
     * @param id the user ID (ignored)
     * @throws RuntimeException always
     */
    @Override
    public void toggleUserStatus(Long id) {
        throw new RuntimeException("Impossible de modifier le statut de l'utilisateur. Le service de sécurité ne répond pas.");
    }

    /**
     * Fallback for retrieving files pending verification (admin only).
     * Returns an empty list.
     *
     * @return an empty ArrayList
     */
    @Override
    public List<Map<String, Object>> getFichiersAVerifier() {
        return new ArrayList<>();
    }

    /**
     * Fallback for AI-based file analysis (admin only).
     * Returns a map with an error message indicating that the AI service is temporarily unavailable.
     *
     * @param request the analysis request (ignored)
     * @return a map with an error key
     */
    @Override
    public Map<String, Object> analyserFichierAvecIA(Map<String, String> request) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("erreur", "L'analyse IA est momentanément indisponible.");
        return fallback;
    }

    // --- Fallbacks for Offer Management via Admin ---

    /**
     * Fallback for retrieving all job offers (admin only).
     * Returns an empty list.
     *
     * @return an empty ArrayList
     */
    @Override
    public List<Map<String, Object>> getAllOffres() {
        return new ArrayList<>();
    }

    /**
     * Fallback for deleting a job offer (admin only).
     * Throws a runtime exception to indicate that the admin service is unavailable.
     *
     * @param id the offer ID (ignored)
     * @throws RuntimeException always
     */
    @Override
    public void supprimerOffre(Long id) {
        throw new RuntimeException("Impossible de supprimer l'offre. Le service Admin est indisponible.");
    }

    /**
     * Fallback for downloading a candidate's CV.
     * Returns a response with HTTP status 503 (Service Unavailable).
     *
     * @param candidatId the candidate ID (ignored)
     * @return a ResponseEntity with status SERVICE_UNAVAILABLE
     */
    @Override
    public ResponseEntity<Resource> downloadCv(Long candidatId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    /**
     * Fallback for downloading a company's logo.
     * Returns a response with HTTP status 503 (Service Unavailable).
     *
     * @param entrepriseId the company ID (ignored)
     * @return a ResponseEntity with status SERVICE_UNAVAILABLE
     */
    @Override
    public ResponseEntity<Resource> downloadLogo(Long entrepriseId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}