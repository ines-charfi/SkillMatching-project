package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.AuthClient;
import com.ines.frontend_skillmatch.service.SessionService;
import com.ines.frontend_skillmatch.service.client.CandidatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    private final SessionService sessionService;
    private final AuthClient authClient;
    private final CandidatClient candidatClient;

    public AdminController(SessionService sessionService, AuthClient authClient, CandidatClient candidatClient) {
        this.sessionService = sessionService;
        this.authClient = authClient;
        this.candidatClient = candidatClient;
    }

    // PAGE PRINCIPALE : DASHBOARD AVEC STATS
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> stats = authClient.getAdminStats();
            model.addAttribute("stats", stats);

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
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            List<Map<String, Object>> users = authClient.getAllUsers();
            model.addAttribute("users", users.stream()
                    .filter(u -> u != null && "CANDIDAT".equals(u.get("role")))
                    .toList());
        } catch (Exception e) {
            model.addAttribute("users", new ArrayList<>());
        }

        model.addAttribute("activeTab", "candidats");
        return "dashboard-admin";
    }

    // GESTION DES ENTREPRISES
    @GetMapping("/admin/entreprises")
    public String adminEntreprises(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            List<Map<String, Object>> users = authClient.getAllUsers();
            model.addAttribute("users", users.stream()
                    .filter(u -> u != null && "ENTREPRISE".equals(u.get("role")))
                    .toList());
        } catch (Exception e) {
            model.addAttribute("users", new ArrayList<>());
        }

        model.addAttribute("activeTab", "entreprises");
        return "dashboard-admin";
    }

    // VÉRIFICATION DES FICHIERS (CV / LOGOS) VIA IA
    @GetMapping("/admin/verification")
    public String adminVerification(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            model.addAttribute("fichiers", authClient.getFichiersAVerifier());
        } catch (Exception e) {
            model.addAttribute("fichiers", new ArrayList<>());
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