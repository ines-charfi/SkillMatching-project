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
                // 1. Désactiver le CSRF pour permettre les requêtes POST de ton formulaire
                .csrf(csrf -> csrf.disable())

                // 2. Autoriser l'accès libre à tes routes
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/login", "/register",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/offre", "/candidats", "/oauth2/**",
                                "/dashboard-candidat", "/dashboard-entreprise" // Ajoute tes dashboards ici
                        ).permitAll()
                        .anyRequest().permitAll() // Autorise tout pour le debug de ton PFE
                )

                // 3. TRÈS IMPORTANT : Désactiver le formLogin de Spring Security
                // car c'est TOI qui gères le login dans ton LoginController
                .formLogin(form -> form.disable())

                // 4. Désactiver le logout par défaut pour utiliser le tien
                .logout(logout -> logout.disable());

        return http.build();
    }
}