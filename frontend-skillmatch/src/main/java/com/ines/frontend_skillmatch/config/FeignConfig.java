package com.ines.frontend_skillmatch.config;

import com.ines.frontend_skillmatch.service.SessionService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor(SessionService sessionService) {
        return requestTemplate -> {
            String token = sessionService.getToken();
            if (token != null) {
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}