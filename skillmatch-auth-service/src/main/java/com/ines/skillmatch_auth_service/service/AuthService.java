package com.ines.skillmatch_auth_service.service;
import com.ines.skillmatch_auth_service.dto.*;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.UserRepository;
import com.ines.skillmatch_auth_service.security.JwtService;
import com.ines.skillmatch_auth_service.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider("LOCAL")
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Générer le token
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .message("Inscription réussie")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .email(userDetails.getEmail())
                    .role(userDetails.getRole().name())
                    .userId(userDetails.getId())
                    .message("Connexion réussie")
                    .build();

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (DisabledException e) {
            throw new RuntimeException("Compte désactivé. Contactez l'administrateur.");
        }
    }

    public AuthResponse processOAuth2Login(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .message("Connexion OAuth2 réussie")
                .build();
    }

    public void logout(String token) {
        // Avec JWT, on peut blacklister le token ou simplement le supprimer côté client
        // Pour l'instant, on ne fait rien côté serveur
    }
}
