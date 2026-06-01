package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.AuthClient;
import com.ines.frontend_skillmatch.service.SessionService;
import com.ines.frontend_skillmatch.service.client.CandidatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    private final SessionService sessionService;
    private final AuthClient authClient; // Injecte le client Feign
    private final CandidatClient candidatClient;

    public AdminController(SessionService sessionService, AuthClient authClient,CandidatClient candidatClient) {
        this.sessionService = sessionService;
        this.authClient = authClient;
        this.candidatClient = candidatClient;
    }

    // PAGE PRINCIPALE : DASHBOARD AVEC STATS (Maquette 6)
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) {
            return "redirect:/login";
        }

        try {
            // Récupération des stats (Candidats Totaux, Entreprises Totales, Offres Publiées)
            Map<String, Object> stats = authClient.getAdminStats();
            model.addAttribute("stats", stats);

            // Récupération de tous les utilisateurs pour le tableau
            List<Map<String, Object>> users = authClient.getAllUsers();
            model.addAttribute("users", users);

            model.addAttribute("activeTab", "dashboard");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de connexion au service Admin");
        }

        return "dashboard-admin";
    }

    // GESTION DES CANDIDATS
    @GetMapping("/admin/candidats")
    public String adminCandidats(Model model) {
        if (!sessionService.isAdmin()) return "redirect:/login";

        List<Map<String, Object>> users = authClient.getAllUsers();
        // On ne garde que les candidats
        model.addAttribute("users", users.stream()
                .filter(u -> "CANDIDAT".equals(u.get("role")))
                .toList());

        model.addAttribute("activeTab", "candidats");
        return "dashboard-admin";
    }

    // GESTION DES ENTREPRISES
    @GetMapping("/admin/entreprises")
    public String adminEntreprises(Model model) {
        if (!sessionService.isAdmin()) return "redirect:/login";

        List<Map<String, Object>> users = authClient.getAllUsers();
        model.addAttribute("users", users.stream()
                .filter(u -> "ENTREPRISE".equals(u.get("role")))
                .toList());

        model.addAttribute("activeTab", "entreprises");
        return "dashboard-admin";
    }

    // VÉRIFICATION DES FICHIERS (CV / LOGOS) via IA
    @GetMapping("/admin/verification")
    public String adminVerification(Model model) {
        if (!sessionService.isAdmin()) return "redirect:/login";

        try {
            model.addAttribute("fichiers", authClient.getFichiersAVerifier());
        } catch (Exception e) {
            model.addAttribute("error", "Service de vérification indisponible");
        }

        model.addAttribute("activeTab", "verification");
        return "dashboard-admin";
    }

    @PostMapping("/admin/users/toggle")
    public String toggleUser(@RequestParam Long id) {
        authClient.toggleUserStatus(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/candidat/valider")
    public String validerCandidat(@RequestParam Long id, @RequestParam String statut) {

        candidatClient.updateValidation(id, statut);
        return "redirect:/admin/verification";
    }
}