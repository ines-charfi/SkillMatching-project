package com.ines.frontend_skillmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // 1. Accueil, Ressources Statiques et Dépendances
                                "/", "/home", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico",
                                "/static/**", "/resources/**",

                                // 2. Authentification et gestion des erreurs
                                "/login", "/register", "/logout", "/logout-user", "/oauth2/**", "/login-error",

                                // 3. Pages Candidat (🔥 Sécurisé avec des patterns larges globaux)
                                "/dashboard-candidat", "/dashboard-candidat/**",
                                "/profil", "/profil/**", "/profil/update",
                                "/mes-candidatures", "/mes-candidatures/**", "/postuler", "/candidat/**",

                                // 4. Pages Entreprise
                                "/dashboard-entreprise", "/dashboard-entreprise/**",
                                "/profil-entreprise", "/profil-entreprise/update",
                                "/offre", "/offre/**",
                                "/offre/nouveau", "/offre/creer",
                                "/offre/supprimer/**", "/offre/modifier/**", "/offre/update/**",
                                "/candidatures/statut", "/candidature/statut",
                                "/entreprise/entretiens/planifier",
                                "/candidat/profil/**", "/candidats",

                                // 5. Technique & Erreurs globales
                                "/actuator/health", "/error",

                                // 6. Uploader de fichiers
                                "/api/candidats/download/cv/**",
                                "/api/entreprises/**",
                                "/api/candidats/avatar/**",

                                // 7. Pages d'Administration
                                "/admin", "/admin/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}