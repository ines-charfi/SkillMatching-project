package com.ines.frontend_skillmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Main security configuration for the frontend application.
 * Defines access control rules, public routes, and session management.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the HTTP security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disabling CSRF and CORS for the frontend's specific communication requirements
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                // 1. Home, Static Assets, and Dependencies
                                "/", "/home", "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico",
                                "/static/**", "/resources/**",

                                // 2. Authentication and Error Pages
                                "/login", "/register", "/logout", "/logout-user", "/oauth2/**", "/login-error",

                                // 3. Candidate Views (Secured via broad global patterns)
                                "/dashboard-candidat", "/dashboard-candidat/**",
                                "/profil", "/profil/**", "/profil/update",
                                "/mes-candidatures", "/mes-candidatures/**", "/postuler", "/candidat/**",

                                // 4. Enterprise/Company Views
                                "/dashboard-entreprise", "/dashboard-entreprise/**",
                                "/profil-entreprise", "/profil-entreprise/update",
                                "/offre", "/offre/**","/offres/**",
                                "/offre/nouveau", "/offre/creer",
                                "/offre/supprimer/**", "/offre/modifier/**", "/offre/update/**",
                                "/candidatures/statut", "/candidature/statut",
                                "/entreprise/entretiens/planifier",
                                "/candidat/profil/**", "/candidats",

                                // 5. Technical Endpoints & Global Error Handling
                                "/actuator/health", "/error",

                                // 6. File Uploading & Media Access
                                "/api/candidats/download/cv/**",
                                "/api/entreprises/**",
                                "/api/candidats/avatar/**",

                                // 7. Administration Pages
                                "/admin", "/admin/**"
                        ).permitAll() // All above routes are accessible without prior authentication

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                // Disabling default Form Login and Http Basic since auth is handled via custom logic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Logout configuration: handle session invalidation and redirection
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