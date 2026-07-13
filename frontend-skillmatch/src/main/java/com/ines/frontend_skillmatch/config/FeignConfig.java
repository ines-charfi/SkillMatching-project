package com.ines.frontend_skillmatch.config;

import com.ines.frontend_skillmatch.service.SessionService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenFeign clients.
 * This class is used to customize the behavior of inter-service HTTP requests.
 */
@Configuration
public class FeignConfig {

    /**
     * Creates a RequestInterceptor to handle security token propagation.
     * It ensures that every outgoing Feign request includes the user's JWT token
     * in the Authorization header.
     *
     * @param sessionService The service used to retrieve the current session's token.
     * @return A RequestInterceptor for adding the Bearer token to headers.
     */
    @Bean
    public RequestInterceptor requestInterceptor(SessionService sessionService) {
        return requestTemplate -> {
            // Retrieve the JWT token from the active session
            String token = sessionService.getToken();

            // If a token is available, inject it into the "Authorization" header using the Bearer scheme
            if (token != null) {
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}