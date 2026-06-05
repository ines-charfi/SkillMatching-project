package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.AuthClient;
import com.ines.frontend_skillmatch.service.SessionService;
import com.ines.frontend_skillmatch.service.client.CandidatClient;
import com.ines.frontend_skillmatch.service.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SessionService sessionService;
    private final AuthClient authClient;
    private final CandidatClient candidatClient;
    private final NotificationClient notificationClient;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        try {
            Long adminId = 1L;

            Map<String, Object> stats = authClient.getGlobalStats();
            List<Map<String, Object>> offres = authClient.getAllOffres();
            List<Map<String, Object>> users = authClient.getAllUsers();

            if (stats == null) {
                stats = new HashMap<>();
            }
            if (offres != null && (!stats.containsKey("totalOffres") || Integer.parseInt(stats.get("totalOffres").toString()) == 0)) {
                stats.put("totalOffres", offres.size());
            }

            model.addAttribute("stats", stats);
            model.addAttribute("users", users);
            model.addAttribute("notifCount", notificationClient.countNonLues(adminId, "admin"));
            model.addAttribute("notifications", notificationClient.getNotifications(adminId, "admin"));
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
    public String toggleUser(@RequestParam("id") Long id, @RequestParam(value = "fromTab", required = false, defaultValue = "") String fromTab) {
        try {
            authClient.toggleUserStatus(id);
        } catch (Exception ignored) {}

        if (fromTab == null || fromTab.isEmpty() || "dashboard".equalsIgnoreCase(fromTab)) {
            return "redirect:/admin";
        }
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

    /**
     * NOUVEAUTÉ : Traitement de l'arbitrage humain via GET pour éliminer les erreurs 400.
     * URL d'appel attendue : /admin/arbitrage/{id}/{statut}/{typeFichier}?userId=XXX
     */
    @GetMapping("/admin/arbitrage")
    public String arbitrerFichier(
            @RequestParam("id") Long id,
            @RequestParam("statut") String statut,
            @RequestParam(value = "typeFichier", defaultValue = "CV") String typeFichier,
            @RequestParam(value = "userId", required = false) Long userId) {

        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        // LOGGER DE SECOURS : Pour voir exactement ce que le contrôleur reçoit dans ta console !
        System.out.println("[ADMIN ARBITRAGE] Reçu -> id: " + id + ", statut: " + statut + ", typeFichier: " + typeFichier + ", userId: " + userId);

        try {
            if ("LOGO".equalsIgnoreCase(typeFichier)) {
                // entrepriseClient.updateValidation(id, statut);
            } else {
                candidatClient.updateValidation(id, statut);
            }

            if ("VALIDE".equalsIgnoreCase(statut) && userId != null) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("userIdTarget", userId);

                if ("LOGO".equalsIgnoreCase(typeFichier)) {
                    notif.put("recipientRole", "entreprise");
                    notif.put("titreNotif", "Logo d'entreprise validé !");
                    notif.put("message", "Le logo de votre structure a été approuvé par l'administration.");
                } else {
                    notif.put("recipientRole", "candidate");
                    notif.put("titreNotif", "Profil Validé !");
                    notif.put("message", "Félicitations, votre profil SkillMatch a été approuvé par l'administrateur.");
                }

                notificationClient.envoyerNotification(notif);
            }
        } catch (Exception e) {
            System.err.println("[ADMIN ARBITRAGE] Erreur microservice : " + e.getMessage());
            e.printStackTrace();
        }
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