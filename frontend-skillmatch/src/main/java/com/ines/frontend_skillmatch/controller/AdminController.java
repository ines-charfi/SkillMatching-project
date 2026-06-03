package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.AuthClient;
import com.ines.frontend_skillmatch.service.SessionService;
import com.ines.frontend_skillmatch.service.client.CandidatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource; // CORRIGÉ : On utilise le bon import Spring ici !
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SessionService sessionService;
    private final AuthClient authClient;
    private final CandidatClient candidatClient;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            model.addAttribute("stats", authClient.getGlobalStats());
            model.addAttribute("users", authClient.getAllUsers());
            model.addAttribute("activeTab", "dashboard");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur service Admin : " + e.getMessage());
        }
        return "dashboard-admin";
    }

    @GetMapping("/admin/candidats")
    public String adminCandidats(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            List<Map<String, Object>> users = authClient.getAllUsers();
            model.addAttribute("users", users.stream().filter(u -> u != null && "CANDIDAT".equals(u.get("role"))).toList());
        } catch (Exception e) {
            model.addAttribute("users", new ArrayList<>());
        }
        model.addAttribute("activeTab", "candidats");
        return "dashboard-admin";
    }

    @GetMapping("/admin/entreprises")
    public String adminEntreprises(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            List<Map<String, Object>> users = authClient.getAllUsers();
            model.addAttribute("users", users.stream().filter(u -> u != null && "ENTREPRISE".equals(u.get("role"))).toList());
        } catch (Exception e) {
            model.addAttribute("users", new ArrayList<>());
        }
        model.addAttribute("activeTab", "entreprises");
        return "dashboard-admin";
    }

    @GetMapping("/admin/offres")
    public String adminOffres(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            model.addAttribute("offres", authClient.getAllOffres());
        } catch (Exception e) {
            model.addAttribute("offres", new ArrayList<>());
        }
        model.addAttribute("activeTab", "offres");
        return "dashboard-admin";
    }

    @GetMapping("/admin/verification")
    public String adminVerification(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            model.addAttribute("fichiers", authClient.getFichiersAVerifier());
        } catch (Exception e) {
            model.addAttribute("fichiers", new ArrayList<>());
        }
        model.addAttribute("activeTab", "verification");
        return "dashboard-admin";
    }

    @PostMapping("/admin/users/toggle")
    public String toggleUser(@RequestParam("id") Long id, @RequestParam("fromTab") String fromTab) {
        try {
            authClient.toggleUserStatus(id);
        } catch (Exception ignored) {}
        return "redirect:/admin/" + fromTab;
    }

    @PostMapping("/admin/offres/supprimer")
    public String supprimerOffre(@RequestParam("id") Long id) {
        try {
            authClient.supprimerOffre(id);
        } catch (Exception ignored) {}
        return "redirect:/admin/offres";
    }

    @PostMapping("/admin/fichiers/analyser")
    public String analyserFichier(@RequestParam("type") String type, Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";
        try {
            Map<String, Object> resultatIa = authClient.analyserFichierAvecIA(Map.of("type", type));
            model.addAttribute("resultatIa", resultatIa);
            model.addAttribute("fichiers", authClient.getFichiersAVerifier());
        } catch (Exception e) {
            model.addAttribute("error", "Échec de l'analyse IA");
        }
        model.addAttribute("activeTab", "verification");
        return "dashboard-admin";
    }

    @PostMapping("/admin/candidat/valider")
    public String validerCandidat(@RequestParam("id") Long id, @RequestParam("statut") String statut) {
        try {
            candidatClient.updateValidation(id, statut);
        } catch (Exception ignored) {}
        return "redirect:/admin/verification";
    }

    @GetMapping("/admin/fichiers/download-cv")
    public ResponseEntity<Resource> downloadCv(@RequestParam("id") Long id) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return authClient.downloadCv(id);
    }

    @GetMapping("/admin/fichiers/download-logo")
    public ResponseEntity<Resource> downloadLogo(@RequestParam("id") Long id) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return authClient.downloadLogo(id);
    }
}