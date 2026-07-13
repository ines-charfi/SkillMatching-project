package com.ines.skillmatch_api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the API Gateway.
 * This is crucial because the frontend (running on a different origin/port)
 * needs to make AJAX calls to the backend services through the gateway.
 * Without this, browsers would block requests due to the Same-Origin Policy.
 */
@Configuration
public class CorsConfig {

    /**
     * Creates a WebFlux CORS filter that applies the configured policy
     * to all incoming requests through the gateway.
     *
     * @return a CorsWebFilter bean that will be automatically applied
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        // Create a new CORS configuration object
        CorsConfiguration config = new CorsConfiguration();

        // 1. ALLOWED ORIGINS
        // Specify which frontend origins are permitted to access the API.
        // In dev, the frontend runs on localhost:8086. In production, this
        // should be updated with the actual domain(s).
        config.setAllowedOrigins(Arrays.asList("http://localhost:8086"));

        // 2. ALLOWED HTTP METHODS
        // Define which HTTP methods are allowed for cross-origin requests.
        // 'OPTIONS' is required for preflight requests (CORS handshake).
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. ALLOWED HEADERS
        // Allow all request headers, including custom ones like 'Authorization'
        // which carries the JWT token. Using "*" is convenient but can be
        // restricted in production for tighter security.
        config.setAllowedHeaders(Arrays.asList("*"));

        // 4. ALLOW CREDENTIALS
        // Critical: permits cookies, HTTP authentication, and client-side
        // SSL certificates to be included in cross-origin requests.
        // This is necessary because the frontend sends the JWT token
        // in the 'Authorization' header (or via cookies) and expects
        // the backend to send Set-Cookie responses.
        config.setAllowCredentials(true);

        // Register the CORS configuration for all routes ("/**")
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // Return the filter that will be applied to every request
        return new CorsWebFilter(source);
    }
}