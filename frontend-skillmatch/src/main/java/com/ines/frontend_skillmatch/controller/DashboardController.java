package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.ApiService;
import com.ines.frontend_skillmatch.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ApiService apiService;
    private final SessionService sessionService;

    public DashboardController(ApiService apiService, SessionService sessionService) {
        this.apiService = apiService;
        this.sessionService = sessionService;
    }

    // ============================================
    // PAGES CANDIDAT
    // ============================================
    @GetMapping("/dashboard-candidat")
    public String dashboardCandidat(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";

        String token = sessionService.getToken();
        Long userId = sessionService.getUserId();

        model.addAttribute("profil", new HashMap<>());
        model.addAttribute("offres", new ArrayList<>());
        model.addAttribute("candidatures", new ArrayList<>());

        try {
            Map profil = apiService.getProfilCandidat(userId, token);
            if (profil != null) model.addAttribute("profil", profil);

            List offres = apiService.getAllOffres(token);
            if (offres != null) model.addAttribute("offres", offres);

            List candidatures = apiService.getMesCandidatures(userId, token);
            if (candidatures != null) model.addAttribute("candidatures", candidatures);
        } catch (Exception e) {
            System.err.println("Erreur chargement données dashboard: " + e.getMessage());
        }

        return "dashboard-candidat";
    }
    // L'URL DOIT ÊTRE /profil car c'est ce que tu appelles depuis le HTML
    @GetMapping("/profil")
    public String profilPage(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";

        String token = sessionService.getToken();
        Long userId = sessionService.getUserId();

        // 1. Préparer une Map avec des valeurs par défaut pour éviter le crash
        Map<String, Object> profilDefault = new HashMap<>();
        profilMap(profilDefault); // méthode utilitaire ci-dessous

        try {
            Map<String, Object> profilReal = apiService.getProfilCandidat(userId, token);
            if (profilReal != null) {
                model.addAttribute("profil", profilReal);
            } else {
                model.addAttribute("profil", profilDefault);
            }
        } catch (Exception e) {
            model.addAttribute("profil", profilDefault);
        }

        return "profil-candidat"; // NOM EXACT DU FICHIER SANS LE .HTML
    }

    // Méthode utilitaire pour remplir la Map si le candidat est nouveau
    private void profilMap(Map<String, Object> map) {
        map.put("prenom", "");
        map.put("nom", "");
        map.put("bio", "");
        map.put("competences", "");
        map.put("niveauScolaire", "Bac+3");
        map.put("cvPath", null);
    }

    // Dans DashboardController.java
    @PostMapping("/profil/update")
    public String handleProfilUpdate(@RequestParam String prenom,
                                     @RequestParam String nom,
                                     @RequestParam(required = false) String telephone,
                                     @RequestParam(required = false) String ville,
                                     @RequestParam(required = false) String adresse,
                                     @RequestParam(required = false) String bio,
                                     @RequestParam(required = false) String competences,
                                     @RequestParam(required = false) String niveauScolaire,
                                     @RequestParam(required = false) org.springframework.web.multipart.MultipartFile cv,
                                     RedirectAttributes ra) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";

        try {
            String token = sessionService.getToken();
            Long userId = sessionService.getUserId();

            // APPEL AVEC LES 11 ARGUMENTS DANS LE BON ORDRE
            apiService.updateProfilCandidatFull(
                    userId, prenom, nom, telephone, ville, adresse,
                    bio, competences, niveauScolaire, cv, token
            );

            ra.addFlashAttribute("message", "Profil mis à jour avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur Update Profil: " + e.getMessage());
            ra.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/dashboard-candidat";
    }

    @GetMapping("/mes-candidatures")
    public String mesCandidatures(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";

        String token = sessionService.getToken();
        Long userId = sessionService.getUserId();

        try {
            model.addAttribute("candidatures", apiService.getMesCandidatures(userId, token));
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de chargement");
        }

        return "mes-candidatures";
    }
    // ============================================
// ACTION : POSTULER
// ============================================
    @PostMapping("/postuler")
    public String postuler(@RequestParam Long offreId, RedirectAttributes redirectAttributes) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";

        String token = sessionService.getToken();
        Long userId = sessionService.getUserId();

        try {
            apiService.postuler(userId, offreId, token);
            redirectAttributes.addFlashAttribute("message", "Candidature envoyée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de postuler : " + e.getMessage());
        }

        return "redirect:/dashboard-candidat";
    }

    // ============================================
    // PAGES ENTREPRISE
    // ============================================
    @GetMapping("/dashboard-entreprise")
    public String dashboardEntreprise(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";
        if (!sessionService.isEntreprise()) return "redirect:" + sessionService.getRedirectUrlByRole();

        String token = sessionService.getToken();
        Long userId = sessionService.getUserId();

        try {
            Map profil = apiService.getProfilEntreprise(userId, token);
            model.addAttribute("profil", profil);

            if (profil != null && profil.get("id") != null) {
                Long entrepriseId = ((Number) profil.get("id")).longValue();
                model.addAttribute("offres", apiService.getOffresByEntreprise(entrepriseId, token));
                model.addAttribute("stats", apiService.getStatsEntreprise(entrepriseId, token));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de chargement des données");
        }

        return "dashboard-entreprise";
    }

    @GetMapping("/profil-entreprise")
    public String profilEntreprise(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";

        String token = sessionService.getToken();
        Long userId = sessionService.getUserId();

        try {
            model.addAttribute("profil", apiService.getProfilEntreprise(userId, token));
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de chargement");
        }

        return "profil-entreprise";
    }

    // ============================================
    // PAGES COMMUNES
    // ============================================
    @GetMapping("/offre")
    public String offres(Model model) {
        String token = sessionService.getToken();

        try {
            model.addAttribute("offres", apiService.getAllOffres(token));
        } catch (Exception e) {
            model.addAttribute("offres", List.of());
        }

        return "offre";
    }

    @GetMapping("/candidats")
    public String candidats(Model model) {
        String token = sessionService.getToken();

        try {
            model.addAttribute("candidats", apiService.getAllOffres(token));
        } catch (Exception e) {
            model.addAttribute("candidats", List.of());
        }

        return "candidats";
    }
}