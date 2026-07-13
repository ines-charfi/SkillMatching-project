package com.ines.skillmatch_auth_service.service;

import com.ines.skillmatch_auth_service.dto.*;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import com.ines.skillmatch_auth_service.security.JwtService;
import com.ines.skillmatch_auth_service.security.UserDetailsImpl;
import com.ines.skillmatch_auth_service.service.client.CandidatClient;
import com.ines.skillmatch_auth_service.service.client.EntrepriseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
// Forces JPA to scan only the 'jpa' package for repositories (avoids conflicts with MongoDB).
@EnableJpaRepositories(basePackages = "com.ines.skillmatch_auth_service.repository.jpa")
// Forces MongoDB to scan only the 'mongodb' package for repositories.
@EnableMongoRepositories(basePackages = "com.ines.skillmatch_auth_service.repository.mongodb")
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CandidatClient candidatClient;
    private final EntrepriseClient entrepriseClient;

    /**
     * Registers a new user account.
     * - Validates email uniqueness.
     * - Encrypts the password using BCrypt.
     * - Saves the user in the database.
     * - Initializes the corresponding profile in the candidate or enterprise service.
     * - Generates a JWT token and returns it.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if the email is already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Build and save the new user entity (password is hashed)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider("LOCAL")
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Initialize the user profile in the appropriate microservice (Candidat or Entreprise)
        try {
            if (user.getRole() == User.Role.CANDIDAT) {
                candidatClient.initCandidat(user.getId(), request.getNom(), request.getPrenom());
            } else if (user.getRole() == User.Role.ENTREPRISE) {
                // Note: Using candidatClient as a fallback – should be replaced with entrepriseClient.initEntreprise()
                candidatClient.initCandidat(user.getId(), request.getNomEntreprise(), "");
            }
        } catch (Exception e) {
            // Log the error but don't block registration – the user account exists even if profile init fails
            log.error("Erreur communication inter-service: {}", e.getMessage());
        }

        // Generate JWT token and build the response
        return AuthResponse.builder()
                .token(jwtService.generateToken(UserDetailsImpl.build(user)))
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .message("Inscription réussie")
                .build();
    }

    /**
     * Authenticates a user with email and password.
     * - Delegates to Spring Security's AuthenticationManager.
     * - If credentials are valid, generates a JWT token.
     * - Returns the token along with user info.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return AuthResponse.builder()
                .token(jwtService.generateToken(userDetails))
                .email(userDetails.getEmail())
                .role(userDetails.getRole().name())
                .userId(userDetails.getId())
                .message("Connexion réussie")
                .build();
    }

    /**
     * Handles logout.
     * With stateless JWT, token invalidation is managed client-side (the client simply discards the token).
     * Server-side blacklisting can be added later if needed.
     */
    public void logout(String token) {
        // No server-side action required for stateless JWT
        // Token blacklisting could be implemented here in the future
    }
}