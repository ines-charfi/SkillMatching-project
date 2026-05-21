package com.ines.frontend_skillmatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    /**
     * Page de connexion OAuth2 réussie
     * Appelée après une connexion Google ou GitHub
     */
    @GetMapping("/oauth2/success")
    public String oauth2Success(@RequestParam String token,
                                @RequestParam String email,
                                @RequestParam String role,
                                @RequestParam(required = false) Long userId,
                                Model model) {
        model.addAttribute("token", token);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("userId", userId);
        return "oauth2-callback";
    }

    /**
     * Page d'erreur de connexion
     */
    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("error", "Erreur lors de la connexion. Veuillez réessayer.");
        return "login";
    }

    /**
     * Page d'erreur OAuth2
     */
    @GetMapping("/oauth2/error")
    public String oauth2Error(Model model) {
        model.addAttribute("error", "Erreur lors de la connexion avec le réseau social.");
        return "login";
    }
}
