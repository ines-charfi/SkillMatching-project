package com.ines.skillmatch_auth_service.config;

import com.ines.skillmatch_auth_service.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactivation de CSRF et CORS pour les requêtes API / Feign REST
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())

                // 2. Mode Stateless (pas de session stockée côté serveur)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Gestion des autorisations des routes
                .authorizeHttpRequests(auth -> auth
                        // Autorise l'accès complet aux endpoints d'authentification et au healthcheck
                        .requestMatchers("/api/auth/**", "/actuator/health","/error").permitAll()

                        // Ajoute cette ligne exacte pour ouvrir la route des notifications :
                        .requestMatchers("/api/notifications/**").permitAll()

                        // Sécurisation de l'API d'administration
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // Tout le reste requiert une authentification valide
                        .anyRequest().authenticated()
                )

                // 4. Injection du filtre JWT avant le filtre d'authentification classique
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}