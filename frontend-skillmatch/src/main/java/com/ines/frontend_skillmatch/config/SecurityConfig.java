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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // 1. Accueil et Statiques
                                "/", "/home", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico",

                                // 2. Authentification et gestion des erreurs
                                "/login", "/register", "/logout-user", "/oauth2/**", "/login-error",

                                // 3. Pages Candidat
                                "/dashboard-candidat", "/profil", "/profil/update", "/mes-candidatures", "/postuler",

                                // 4. Pages Entreprise (CORRIGÉ & COMPLET)
                                "/dashboard-entreprise",
                                "/profil-entreprise",          // 💡 Débloque l'affichage du profil entreprise
                                "/profil-entreprise/update",   // 💡 Débloque la modification du profil
                                "/offre",                      // Liste générale des offres
                                "/offre/nouveau",              // 💡 Débloque le formulaire de création
                                "/offre/creer",                // 💡 Débloque la soumission de l'offre
                                "/offre/supprimer/**",
                                "/offre/modifier/**",          // 🎯 AJOUT : Autorise l'affichage du formulaire de modification
                                "/offre/update/**",            // 🎯 AJOUT : Autorise la soumission des modifications (POST)// 💡 Débloque la suppression d'offres
                                "/candidatures/statut",// 💡 Débloque les boutons Accepter/Refuser
                                "/candidature/statut",
                                "/entreprise/entretiens/planifier",
                                "/candidat/profil/**",
                                "/candidats",

                                // 5. Technique & Erreurs globales
                                "/actuator/health", "/error",

                                // 6. Uploader cv et avatar
                                "/api/candidats/download/cv/**",
                                "/api/entreprises/**", "/api/candidats/avatar/**"

                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}