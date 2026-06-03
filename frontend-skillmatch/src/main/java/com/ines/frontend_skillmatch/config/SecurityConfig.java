package com.ines.frontend_skillmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactivé pour laisser passer les formulaires Thymeleaf librement
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // 1. Accueil, Ressources Statiques et Dépendances
                                "/", "/home", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico",
                                "/static/**", "/resources/**",

                                // 2. Authentification et gestion des erreurs
                                "/login", "/register", "/logout-user", "/oauth2/**", "/login-error",

                                // 3. Pages Candidat
                                "/dashboard-candidat", "/profil", "/profil/update", "/mes-candidatures", "/postuler",

                                // 4. Pages Entreprise
                                "/dashboard-entreprise",
                                "/profil-entreprise",
                                "/profil-entreprise/update",
                                "/offre",
                                "/offre/nouveau",
                                "/offre/creer",
                                "/offre/supprimer/**",
                                "/offre/modifier/**",
                                "/offre/update/**",
                                "/candidatures/statut",
                                "/candidature/statut",
                                "/entreprise/entretiens/planifier",
                                "/candidat/profil/**",
                                "/candidats",

                                // 5. Technique & Erreurs globales
                                "/actuator/health", "/error",

                                // 6. Uploader de fichiers (CV, avatars, dossiers entreprises)
                                "/api/candidats/download/cv/**",
                                "/api/entreprises/**",
                                "/api/candidats/avatar/**",

                                // 7. Pages d'Administration (Aiguillage sécurisé par ton SessionService)
                                "/admin", "/admin/**"

                        ).permitAll()

                        // Tout le reste de l'application (s'il y a des routes oubliées) requiert une authentification
                        .anyRequest().authenticated()
                )
                // On désactive les formulaires et fenêtres pop-up natifs de Spring Security
                // pour que ton LoginController personnalisé garde la main à 100%
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}