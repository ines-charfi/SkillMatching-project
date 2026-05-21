package com.ines.frontend_skillmatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirections simples
        registry.addViewController("/").setViewName("redirect:/login");
        registry.addRedirectViewController("/dashboard", "/dashboard-candidat");
    }
}
