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

/**
 * Controller responsible for administrative management.
 * It acts as a bridge between the frontend and various microservices (Auth, Candidat, Notification).
 */
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SessionService sessionService;
    private final AuthClient authClient;
    private final CandidatClient candidatClient;
    private final NotificationClient notificationClient;

    /**
     * ENDPOINT: GET /admin
     * FUNCTION: Displays the main administrator dashboard.
     * LOGIC:
     * 1. Checks if the user is authenticated and has ADMIN rights.
     * 2. Fetches global platform statistics (users, offers).
     * 3. Retrieves recent notifications for the admin.
     * 4. Updates the model with statistics and the full user list.
     */
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

    /**
     * ENDPOINT: GET /admin/candidats
     * FUNCTION: Displays the list of all registered candidates.
     * LOGIC: Fetches all users and filters them to only keep those with the "CANDIDAT" role.
     */
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

    /**
     * ENDPOINT: GET /admin/entreprises
     * FUNCTION: Displays the list of all registered companies.
     * LOGIC: Fetches all users and filters them to only keep those with the "ENTREPRISE" role.
     */
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

    /**
     * ENDPOINT: GET /admin/offres
     * FUNCTION: Displays all job offers published on the platform.
     * LOGIC: Fetches the complete list of offers via the AuthClient (which communicates with the job service).
     */
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

    /**
     * ENDPOINT: GET /admin/verification
     * FUNCTION: Displays the list of pending verification items (CVs, Logos).
     * LOGIC: Retrieves documents that require administrative approval before being fully active on the platform.
     */
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

    /**
     * ENDPOINT: POST /admin/users/toggle
     * FUNCTION: Enables or disables a user account.
     * LOGIC:
     * 1. Calls the auth service to switch the user status (active <-> inactive).
     * 2. Redirects the admin back to the specific tab they were on (candidats, entreprises, etc.).
     */
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

    /**
     * ENDPOINT: POST /admin/offres/supprimer
     * FUNCTION: Deletes a job offer.
     * LOGIC: Requests the deletion of a specific offer by ID via the inter-service communication client.
     */
    @PostMapping("/admin/offres/supprimer")
    public String supprimerOffre(@RequestParam("id") Long id) {
        try {
            authClient.supprimerOffre(id);
        } catch (Exception ignored) {}
        return "redirect:/admin/offres";
    }

    /**
     * ENDPOINT: POST /admin/fichiers/analyser
     * FUNCTION: Triggers an AI analysis on a specific file type.
     * LOGIC: Sends a request to the AI processing module to automatically verify if a CV or Logo meets platform criteria.
     */
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
     * ENDPOINT: GET /admin/arbitrage
     * FUNCTION: Manually validates or rejects a profile file (CV or Logo).
     * LOGIC:
     * 1. Updates the validation status (VALIDE/REJETE) in the corresponding service (Candidat or Entreprise).
     * 2. If validated, sends a success notification to the concerned user with a specific message based on their role.
     * 3. Redirects back to the verification queue.
     */
    @GetMapping("/admin/arbitrage")
    public String arbitrerFichier(
            @RequestParam("id") Long id,
            @RequestParam("statut") String statut,
            @RequestParam(value = "typeFichier", defaultValue = "CV") String typeFichier,
            @RequestParam(value = "userId", required = false) Long userId) {

        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) return "redirect:/login";

        System.out.println("[ADMIN ARBITRAGE] Reçu -> id: " + id + ", statut: " + statut + ", typeFichier: " + typeFichier + ", userId: " + userId);

        try {
            if ("LOGO".equalsIgnoreCase(typeFichier)) {
                // Logic for company logo validation
            } else {
                candidatClient.updateValidation(id, statut);
            }

            if ("VALIDE".equalsIgnoreCase(statut) && userId != null) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("userIdTarget", userId);
                notif.put("lu", false);

                if ("LOGO".equalsIgnoreCase(typeFichier)) {
                    notif.put("recipientRole", "recruiter");
                    notif.put("titreNotif", "Profil Validé ! 🎉");
                    notif.put("message", "Félicitations ! Votre profil entreprise a été validé par l'administrateur. Vous pouvez désormais publier des offres.");
                } else {
                    notif.put("recipientRole", "candidate");
                    notif.put("titreNotif", "Profil Validé ! 🎉");
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

    /**
     * ENDPOINT: GET /admin/fichiers/download-cv
     * FUNCTION: Proxies the download of a candidate's CV.
     * LOGIC: Verifies admin access then fetches the file resource from the backend storage service.
     */
    @GetMapping("/admin/fichiers/download-cv")
    public ResponseEntity<Resource> downloadCv(@RequestParam("id") Long id) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return authClient.downloadCv(id);
    }

    /**
     * ENDPOINT: GET /admin/fichiers/download-logo
     * FUNCTION: Proxies the download of a company's logo.
     * LOGIC: Verifies admin access then fetches the logo resource from the backend storage service.
     */
    @GetMapping("/admin/fichiers/download-logo")
    public ResponseEntity<Resource> downloadLogo(@RequestParam("id") Long id) {
        if (!sessionService.isAuthenticated() || !sessionService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return authClient.downloadLogo(id);
    }
}