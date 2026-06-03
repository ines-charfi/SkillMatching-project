package com.ines.skillmatch_auth_service.controller;

import com.ines.skillmatch_auth_service.dto.*;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.UserRepository;
import com.ines.skillmatch_auth_service.service.AuthService;
import com.ines.skillmatch_auth_service.service.client.OffreClient;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    // AUTH
    // ============================================

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@RequestParam String token) {
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .message("Connexion OAuth2 réussie")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        return ResponseEntity.ok(Map.of(
                "message", "Vous êtes connecté",
                "timestamp", System.currentTimeMillis()
                // Signé Ines
        ));
    }

    // ============================================
    // ADMIN : Gestion des utilisateurs
    // ============================================

    @GetMapping("/users")
    // 🎯 CORRECTION : hasRole -> hasAuthority pour matcher la chaîne brute "ADMIN" de ta DB
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = users.stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/users/{id}")
    // 🎯 CORRECTION : hasRole -> hasAuthority
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/users/{id}/toggle-status")
    // 🎯 CORRECTION : hasRole -> hasAuthority
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEnabled(!user.getEnabled());
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/users/{id}/role")
    // 🎯 CORRECTION : hasRole -> hasAuthority
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id,
                                                  @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setRole(User.Role.valueOf(role.toUpperCase()));
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    // ============================================
    // NOUVELLE MÉTHODE POUR LA PAGE D'ACCUEIL
    // ============================================
    @GetMapping("/stats/public")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());

        try {
            stats.put("totalOffres", offreClient.countAllOffres());
            stats.put("totalEntreprises", userRepository.countByRole(User.Role.ENTREPRISE));
        } catch (Exception e) {
            stats.put("totalOffres", 0);
            stats.put("totalEntreprises", 0);
        }

        return ResponseEntity.ok(stats);
    }

    // ============================================
    // PRIVATE
    // ============================================

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