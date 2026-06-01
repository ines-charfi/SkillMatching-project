package com.ines.frontend_skillmatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1. Redirige la racine (/) vers le login automatiquement
        // C'est très bien pour éviter d'avoir une page vide au démarrage
        registry.addViewController("/").setViewName("redirect:/login");

        // 2. SUPPRIME ou COMMENTE la ligne du dashboard ici !
        // registry.addRedirectViewController("/dashboard", "/dashboard-candidat");
    }
}