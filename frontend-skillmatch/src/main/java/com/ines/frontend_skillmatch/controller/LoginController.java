package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.AuthClient;
import com.ines.frontend_skillmatch.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Controller responsible for public access and identity management.
 * Handles the home page, user authentication (Login), and account creation (Registration).
 */
@Controller
public class LoginController {

    private final SessionService sessionService;
    private final AuthClient authClient;

    public LoginController(SessionService sessionService, AuthClient authClient) {
        this.sessionService = sessionService;
        this.authClient = authClient;
    }

    /**
     * ENDPOINT: GET /
     * FUNCTION: Displays the public landing page (Home).
     * LOGIC: Fetches and displays global public statistics (Total users and job offers) from the Auth microservice.
     */
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

    /**
     * ENDPOINT: GET /login
     * FUNCTION: Displays the login form.
     * LOGIC:
     * 1. Handles feedback messages for errors, successful logout, or account creation.
     * 2. (Note: Automatic redirection for authenticated users is currently disabled for debugging).
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String success,
                            Model model) {
        // NOTE: Redirection logic is commented out to allow manual testing
        /* if (sessionService.isAuthenticated()) {
            return "redirect:" + sessionService.getRedirectUrlByRole();
        }
        */

        if (error != null) model.addAttribute("error", "Email ou mot de passe incorrect");
        if (logout != null) model.addAttribute("message", "Déconnexion réussie");
        if (success != null) model.addAttribute("successMessage", "Votre compte a été créé avec succès !");

        return "login";
    }

    /**
     * ENDPOINT: POST /login
     * FUNCTION: Processes authentication.
     * LOGIC:
     * 1. Sends credentials to the Auth microservice.
     * 2. If successful, retrieves the JWT token, userId, email, and role.
     * 3. Initializes the user session via SessionService and redirects to the role-based dashboard.
     * 4. In case of failure, logs the technical stack trace for debugging purposes.
     */
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
            // Log real cause in the terminal for developer troubleshooting
            System.err.println("[ FRONTEND DEBUG] L'appel d'authentification a échoué ! Cause réelle :");
            e.printStackTrace();

            model.addAttribute("error", "Erreur technique : " + e.getMessage());
            return "login";
        }
    }

    /**
     * ENDPOINT: GET /register
     * FUNCTION: Displays the user registration form.
     */
    @GetMapping("/register")
    public String registerPage() {
        // Note: Automatic redirection is disabled to allow account creation during active sessions
        /*
        if (sessionService.isAuthenticated()) {
            return "redirect:" + sessionService.getRedirectUrlByRole();
        }
        */
        return "register";
    }

    /**
     * ENDPOINT: POST /register
     * FUNCTION: Processes new user registration.
     * LOGIC:
     * 1. Collects data based on the selected role (Candidat vs Entreprise).
     * 2. Submits data to the AuthClient.
     * 3. Redirects to login page upon success.
     */
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

    /**
     * ENDPOINT: GET /logout-user
     * FUNCTION: Standard user logout.
     * LOGIC: Destroys the custom session through SessionService and redirects with a logout flag.
     */
    @GetMapping("/logout-user")
    public String logout() {
        sessionService.destroySession();
        return "redirect:/login?logout=true";
    }

    /**
     * ENDPOINT: GET /logout
     * FUNCTION: Alternative logout through HttpSession invalidation.
     * LOGIC: Invalidates the underlying HTTP session and redirects to login.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 1. Invalidate custom session management
        session.invalidate();

        // 2. Redirect to login page
        return "redirect:/login?logout";
    }
}