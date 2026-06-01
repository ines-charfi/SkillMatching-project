package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.AuthClient;
import com.ines.frontend_skillmatch.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@Controller
public class LoginController {

    private final SessionService sessionService;
    private final AuthClient authClient;

    public LoginController(SessionService sessionService, AuthClient authClient) {
        this.sessionService = sessionService;
        this.authClient = authClient;
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            Map<String, Object> stats = authClient.getPublicStats();
            model.addAttribute("totalUsers", stats.get("totalUsers"));
            model.addAttribute("totalOffres", stats.get("totalOffres"));
        } catch (Exception e) {
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalOffres", 0);
        }
        return "home";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String success,
                            Model model) {
        // CORRECTION : On commente le "if" pour empêcher la redirection automatique sauvage
        /* if (sessionService.isAuthenticated()) {
            return "redirect:" + sessionService.getRedirectUrlByRole();
        }
        */

        if (error != null) model.addAttribute("error", "Email ou mot de passe incorrect");
        if (logout != null) model.addAttribute("message", "Déconnexion réussie");
        if (success != null) model.addAttribute("successMessage", "Votre compte a été créé avec succès !");

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model) {
        try {
            Map<String, String> credentials = Map.of("email", email, "password", password);
            Map<String, Object> response = authClient.login(credentials);

            if (response != null && response.containsKey("token")) {
                String token = (String) response.get("token");
                String role = (String) response.get("role");
                String userEmail = (String) response.get("email");
                Number userIdNum = (Number) response.get("userId");
                Long userId = (userIdNum != null) ? userIdNum.longValue() : null;

                sessionService.createSession(token, userId, userEmail, role);
                return "redirect:" + sessionService.getRedirectUrlByRole();
            } else {
                model.addAttribute("error", "Identifiants invalides");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Service d'authentification indisponible.");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        // CORRECTION : On commente aussi ici pour pouvoir créer des comptes sans être bloqué
        /*
        if (sessionService.isAuthenticated()) {
            return "redirect:" + sessionService.getRedirectUrlByRole();
        }
        */
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           @RequestParam(required = false) String prenom,
                           @RequestParam(required = false) String nom,
                           @RequestParam(required = false) String nomEntreprise,
                           Model model) {
        try {
            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("email", email);
            registrationData.put("password", password);
            registrationData.put("role", role);

            if ("CANDIDAT".equals(role)) {
                registrationData.put("prenom", prenom);
                registrationData.put("nom", nom);
            } else if ("ENTREPRISE".equals(role)) {
                registrationData.put("nomEntreprise", nomEntreprise);
            }

            authClient.register(registrationData);
            return "redirect:/login?success=true";

        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue lors de l'inscription. L'email est peut-être déjà utilisé.");
            return "register";
        }
    }

    @GetMapping("/logout-user")
    public String logout() {
        sessionService.destroySession();
        return "redirect:/login?logout=true";
    }
}