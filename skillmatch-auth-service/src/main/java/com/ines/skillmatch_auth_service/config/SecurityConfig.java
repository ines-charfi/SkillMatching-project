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

/**
 * Main security configuration for the authentication service.
 * Defines authentication rules, JWT filtering, and session management.
 * Uses stateless JWT-based authentication instead of traditional sessions.
 */
@Configuration
// Enables Spring Security's web security features
@EnableWebSecurity
// Enables method-level security annotations like @PreAuthorize, @Secured
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final PasswordEncoder passwordEncoder;

    /**
     * Configures the main security filter chain.
     * This defines which endpoints are public, which require authentication,
     * and how authentication is processed (stateless JWT).
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. DISABLE CSRF AND CORS
                // CSRF is disabled because JWT tokens are stored in headers, not cookies.
                // CORS is disabled here because the API Gateway handles it globally.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())

                // 2. STATELESS SESSION MANAGEMENT
                // No server-side session is created. Each request must contain a valid JWT.
                // This makes the service stateless and horizontally scalable.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. AUTHORIZATION RULES
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS - accessible without authentication
                        .requestMatchers("/api/auth/**", "/actuator/health", "/error").permitAll()
                        // Notifications endpoints - temporarily opened for testing/frontend access
                        .requestMatchers("/api/notifications/**").permitAll()

                        // ADMIN ENDPOINTS - restricted to users with ADMIN role
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // ALL OTHER REQUESTS - require a valid JWT token
                        .anyRequest().authenticated()
                )

                // 4. JWT FILTER INJECTION
                // Adds the JWT validation filter BEFORE the standard UsernamePasswordAuthenticationFilter.
                // This ensures the token is validated first, so the request is authenticated
                // before reaching the username/password authentication process.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides the AuthenticationManager bean used for authenticating users
     * during login (username/password verification).
     *
     * @param config the AuthenticationConfiguration
     * @return the AuthenticationManager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}