package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.ApiService;
import com.ines.frontend_skillmatch.service.SessionService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class LoginController {

    private final SessionService sessionService;
    private final ApiService apiService;  // AJOUTEZ CETTE LIGNE

    // AJOUTE CE CONSTRUCTEUR MANUELLEMENT ICI
    public LoginController(SessionService sessionService, ApiService apiService) {
        this.sessionService = sessionService;
        this.apiService = apiService;
    }



    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String role,
                            Model model) {

        if (sessionService.isAuthenticated()) {
            return "redirect:" + sessionService.getRedirectUrlByRole();
        }

        if (error != null) {model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("message", "Déconnexion réussie");
        }

        model.addAttribute("role", role != null ? role : "CANDIDAT");
        return "login";
    }

    // ============================================
    // AJOUTEZ CETTE MÉTHODE POUR LA CONNEXION POST
    // ============================================
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        try {
            // 1. Appel API
            Map<String, Object> response = apiService.login(email, password);

            if (response != null && response.containsKey("token")) {
                // 2. Extraction des données
                String token = (String) response.get("token");
                String role = (String) response.get("role");
                String userEmail = (String) response.get("email");

                // Conversion sécurisée de l'ID (JSON donne souvent un Integer, on veut un Long)
                Number userIdNum = (Number) response.get("userId");
                Long userId = (userIdNum != null) ? userIdNum.longValue() : null;

                // 3. UTILISE TON SESSION SERVICE (Très important !)
                // Cela évite les erreurs de clés (user_token, etc.)
                sessionService.createSession(token, userId, userEmail, role);

                System.out.println("DEBUG: Connexion réussie pour " + userEmail + " (Rôle: " + role + ")");

                // 4. REDIRECTION VERS LES DASHBOARDS SPÉCIFIQUES
                if ("CANDIDAT".equalsIgnoreCase(role)) {
                    return "redirect:/dashboard-candidat";
                } else if ("ENTREPRISE".equalsIgnoreCase(role)) {
                    return "redirect:/dashboard-entreprise";
                } else {
                    // Si c'est un autre rôle (ex: ADMIN)
                    return "redirect:" + sessionService.getRedirectUrlByRole();
                }
            } else {
                model.addAttribute("error", "Email ou mot de passe incorrect");
                return "login";
            }
        } catch (Exception e) {
            // Affiche l'erreur dans ta console IntelliJ pour débugger !
            System.err.println("ERREUR LOGIN: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur de connexion : " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        if (sessionService.isAuthenticated()) {
            return "redirect:" + sessionService.getRedirectUrlByRole();
        }
        return "register";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login";
        }
        return sessionService.getDashboardPage();
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam String token,
                                 @RequestParam String email,
                                 @RequestParam String role,
                                 @RequestParam(required = false) Long userId,
                                 Model model) {

        sessionService.createOAuth2Session(token, email, role, userId);

        model.addAttribute("token", token);
        model.addAttribute("email", email);
        model.addAttribute("role", role);

        return "oauth2-callback";
    }

    @GetMapping("/logout-user")
    public String logout() {
        sessionService.destroySession();
        return "redirect:/login?logout=true";
    }
}