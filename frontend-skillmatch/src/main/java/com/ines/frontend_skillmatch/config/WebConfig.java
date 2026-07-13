package com.ines.frontend_skillmatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Custom Web MVC configuration for the frontend application.
 * Handles view-related mappings and automatic redirects.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers view controllers for simple automated redirects or static views.
     *
     * @param registry The controller registry to populate.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1. Automatically redirects the root path (/) to the login page.
        // This ensures users are immediately prompted to authenticate, avoiding a blank landing page.
        registry.addViewController("/").setViewName("redirect:/login");

        // 2. DISABLED: The generic dashboard redirect is commented out/removed.
        // Dashboard routing is now managed dynamically within controllers to support role-based redirection.
        // registry.addRedirectViewController("/dashboard", "/dashboard-candidat");
    }
}