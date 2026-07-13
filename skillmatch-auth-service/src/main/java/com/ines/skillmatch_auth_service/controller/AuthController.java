package com.ines.skillmatch_auth_service.controller;

import com.ines.skillmatch_auth_service.dto.*;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import com.ines.skillmatch_auth_service.service.AuthService;
import com.ines.skillmatch_auth_service.service.client.OffreClient;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication REST Controller.
 * Handles user registration, login, OAuth2, and admin user management.
 * Public endpoints are under /api/auth; admin endpoints are protected with @PreAuthorize.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final OffreClient offreClient;

    public AuthController(AuthService authService, UserRepository userRepository, OffreClient offreClient) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.offreClient = offreClient;
    }

    // ============================================
    // AUTHENTICATION - PUBLIC ENDPOINTS
    // Accessible without authentication
    // ============================================

    /**
     * Registers a new user account.
     * @param request registration data (email, password, role, etc.)
     * @return JWT token and user info on success
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authenticates a user with email and password.
     * @param request login credentials
     * @return JWT token and user info on success
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * OAuth2 success callback endpoint.
     * Called after successful Google/GitHub authentication.
     * @param token the JWT token generated after OAuth2 login
     * @return authentication response with the token
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@RequestParam String token) {
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .message("Connexion OAuth2 réussie")
                .build());
    }

    /**
     * Logs out the current user.
     * The actual token invalidation is handled client-side.
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    /**
     * Returns basic info about the currently authenticated user.
     * Useful for session validation.
     * @return simple user status message
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        return ResponseEntity.ok(Map.of(
                "message", "Vous êtes connecté",
                "timestamp", System.currentTimeMillis()
        ));
    }

    // ============================================
    // ADMIN - USER MANAGEMENT
    // These endpoints require ADMIN authority
    // ============================================

    /**
     * Retrieves all registered users.
     * @return list of UserDto (safe representation without password)
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = users.stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves a specific user by ID.
     * @param id user ID
     * @return UserDto for the requested user
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Toggles the enabled/disabled status of a user account.
     * Used for suspending or reactivating accounts.
     * @param id user ID
     * @return updated UserDto
     */
    @PutMapping("/users/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEnabled(!user.getEnabled());
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Changes the role of a user (e.g., CANDIDAT → ENTREPRISE).
     * @param id user ID
     * @param role new role name (ADMIN, CANDIDAT, ENTREPRISE)
     * @return updated UserDto
     */
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setRole(User.Role.valueOf(role.toUpperCase()));
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    // ============================================
    // PUBLIC STATISTICS
    // Accessible without authentication
    // ============================================

    /**
     * Returns public platform statistics.
     * Aggregates counts from local DB and remote services (with fallback).
     * @return map containing total users, offers, and enterprises
     */
    @GetMapping("/stats/public")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        try {
            stats.put("totalOffres", offreClient.countAllOffres());
            stats.put("totalEntreprises", userRepository.countByRole(User.Role.ENTREPRISE));
        } catch (Exception e) {
            // Fallback: if offer service is down, return 0s to avoid breaking the frontend
            stats.put("totalOffres", 0);
            stats.put("totalEntreprises", 0);
        }
        return ResponseEntity.ok(stats);
    }

    // ============================================
    // UTILITY METHOD
    // ============================================

    /**
     * Converts a User entity to a UserDto (safe DTO without password).
     */
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .provider(user.getProvider())
                .dateCreation(user.getDateCreation())
                .build();
    }
}