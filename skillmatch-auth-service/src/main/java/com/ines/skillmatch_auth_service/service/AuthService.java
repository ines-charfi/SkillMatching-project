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
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final CandidatClient candidatClient;
        private final EntrepriseClient entrepriseClient;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .provider("LOCAL")
                    .enabled(true)
                    .build();

            user = userRepository.save(user);

            // INITIALISATION DU PROFIL DANS LES AUTRES SERVICES
            try {
                if (user.getRole() == User.Role.CANDIDAT) {
                    candidatClient.initCandidat(user.getId(), request.getNom(), request.getPrenom());
                } else if (user.getRole() == User.Role.ENTREPRISE) {
                    candidatClient.initCandidat(user.getId(), request.getNomEntreprise(), "");
                    // Note: Tu peux adapter selon ton EntrepriseClient
                }
            } catch (Exception e) {
                log.error("Erreur communication inter-service: {}", e.getMessage());
            }

            return AuthResponse.builder()
                    .token(jwtService.generateToken(UserDetailsImpl.build(user)))
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .userId(user.getId())
                    .message("Inscription réussie")
                    .build();
        }

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


        public void logout(String token) {
            // Avec JWT, on peut blacklister le token ou simplement le supprimer côté client
            // Pour l'instant, on ne fait rien côté serveur

        }
    }

