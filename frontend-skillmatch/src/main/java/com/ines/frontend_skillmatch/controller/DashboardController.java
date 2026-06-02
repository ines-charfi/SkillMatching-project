package com.ines.frontend_skillmatch.controller;

import com.ines.frontend_skillmatch.service.client.*;
import com.ines.frontend_skillmatch.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur Principal du Frontend de l'application SkillMatch.
 * Gère l'aiguillage des utilisateurs, les espaces Candidat et Entreprise,
 * ainsi que le cycle de vie des offres et des candidatures via des appels Feign.
 */
@Controller
public class DashboardController {

    private final CandidatClient candidatClient;
    private final EntrepriseClient entrepriseClient;
    private final OffreClient offreClient;
    private final CandidatureClient candidatureClient;
    private final SessionService sessionService;

    /**
     * Constructeur avec injection de toutes les dépendances Feign et Services.
     */
    public DashboardController(CandidatClient candidatClient,
                               EntrepriseClient entrepriseClient,
                               OffreClient offreClient,
                               CandidatureClient candidatureClient,
                               SessionService sessionService) {
        this.candidatClient = candidatClient;
        this.entrepriseClient = entrepriseClient;
        this.offreClient = offreClient;
        this.candidatureClient = candidatureClient;
        this.sessionService = sessionService;
    }

    /**
     * Point d'entrée générique du tableau de bord (`/dashboard`).
     */
    @GetMapping("/dashboard")
    public String genericDashboard() {
        if (!sessionService.isAuthenticated()) return "redirect:/login";
        return "redirect:" + sessionService.getRedirectUrlByRole();
    }

    // =========================================================================
    // ESPACE ENTREPRISE / RECRUTEUR
    // =========================================================================

    /**
     * Affiche le tableau de bord principal de l'Espace Entreprise.
     */
    @GetMapping("/dashboard-entreprise")
    public String dashboardEntreprise(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";

        model.addAttribute("profil", new HashMap<>());
        model.addAttribute("offres", new ArrayList<>());
        model.addAttribute("stats", new HashMap<>());
        model.addAttribute("candidaturesRecues", new ArrayList<>());

        try {
            Map<String, Object> profil = entrepriseClient.getByUserId(sessionService.getUserId());
            if (profil != null) {
                model.addAttribute("profil", profil);

                if (profil.get("id") != null) {
                    Long entId = Long.valueOf(profil.get("id").toString());

                    try {
                        model.addAttribute("offres", offreClient.getByEntreprise(entId));
                    } catch (Exception e) {
                    }
                    try {
                        model.addAttribute("stats", candidatureClient.getStatsEntreprise(entId));
                    } catch (Exception e) {
                    }
                    try {
                        model.addAttribute("candidaturesRecues", candidatureClient.getByEntreprise(entId));
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de chargement des données de l'entreprise.");
        }
        return "dashboard-entreprise";
    }

    /**
     * Accède à la page de modification du profil de l'entreprise.
     */
    @GetMapping("/profil-entreprise")
    public String profilEntreprisePage(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> profil = entrepriseClient.getByUserId(sessionService.getUserId());
            model.addAttribute("profil", profil != null ? profil : new HashMap<>());
        } catch (Exception e) {
            model.addAttribute("profil", new HashMap<>());
        }
        return "profil-entreprise";
    }

    /**
     * Traite la soumission du formulaire de mise à jour du profil de l'entreprise.
     */
    @PostMapping("/profil-entreprise/update")
    public String handleEntrepriseProfilUpdate(@RequestParam String nomEntreprise, @RequestParam(required = false) String secteur,
                                               @RequestParam(required = false) String description, @RequestParam(required = false) String contactEmail,
                                               @RequestParam(required = false) String telephone, @RequestParam(required = false) String siteWeb,
                                               @RequestParam(required = false) String ville, @RequestParam(required = false) MultipartFile logo,
                                               RedirectAttributes ra) {
        try {
            entrepriseClient.updateProfil(sessionService.getUserId(), nomEntreprise, secteur, description, siteWeb, telephone, contactEmail, logo);
            ra.addFlashAttribute("message", "Profil entreprise mis à jour avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Échec de la mise à jour du profil entreprise.");
        }
        return "redirect:/dashboard-entreprise";
    }

    // =========================================================================
    // GESTION DES OFFRES (CRUD)
    // =========================================================================

    /**
     * Affiche le formulaire de création d'une nouvelle offre d'emploi.
     */
    @GetMapping("/offre/nouveau")
    public String nouvelleOffreForm(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) {
            return "redirect:/login";
        }
        try {
            Map<String, Object> profil = entrepriseClient.getByUserId(sessionService.getUserId());
            model.addAttribute("profil", profil != null ? profil : new HashMap<>());
        } catch (Exception e) {
            Map<String, Object> profilMock = new HashMap<>();
            profilMock.put("nomEntreprise", "aldiGroup");
            profilMock.put("secteur", "IA DEV WEB");
            model.addAttribute("profil", profilMock);
        }
        return "creer-offre";
    }

    /**
     * Enregistre une nouvelle offre d'emploi dans le système.
     */
    @PostMapping("/offre/creer")
    public String handleOffreCreation(@RequestParam String titre, @RequestParam String description,
                                      @RequestParam String niveauRequis, @RequestParam String salaire,
                                      @RequestParam String competencesRequises, RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> profil = sessionService.getUserId() != null ? entrepriseClient.getByUserId(sessionService.getUserId()) : null;
            Long codebaseId = (profil != null && profil.get("id") != null) ? Long.valueOf(profil.get("id").toString()) : null;

            Map<String, Object> offreData = new HashMap<>();
            offreData.put("entrepriseId", codebaseId);
            offreData.put("titre", titre);
            offreData.put("description", description);
            offreData.put("competencesRequises", competencesRequises);
            offreData.put("niveauRequis", niveauRequis);
            offreData.put("salaire", salaire);

            String villeEntreprise = (profil != null && profil.get("ville") != null) ? profil.get("ville").toString() : "";
            offreData.put("ville", villeEntreprise);
            offreData.put("typeContrat", "CDI");

            offreClient.create(offreData);
            ra.addFlashAttribute("message", "Offre d'emploi publiée avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Échec de la publication de l'offre : " + e.getMessage());
        }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * Récupère une offre d'emploi existante pour la charger dans un formulaire de modification.
     */
    @GetMapping("/offre/modifier/{id}")
    public String modifierOffreForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> profil = entrepriseClient.getByUserId(sessionService.getUserId());
            model.addAttribute("profil", profil != null ? profil : new HashMap<>());

            Map<String, Object> offreCible = (Map<String, Object>) (Object) offreClient.getById(id);

            model.addAttribute("offre", offreCible);
            return "modifier-offre";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de charger l'offre pour modification : " + e.getMessage());
            return "redirect:/dashboard-entreprise";
        }
    }

    /**
     * Traite la soumission du formulaire de modification (Restauration complète de la méthode).
     */
    @PostMapping("/offre/update/{id}")
    public String handleOffreUpdate(@PathVariable("id") Long id,
                                    @RequestParam String titre,
                                    @RequestParam String description,
                                    @RequestParam String niveauRequis,
                                    @RequestParam String salaire,
                                    @RequestParam String competencesRequises,
                                    RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> offreData = new HashMap<>();
            offreData.put("titre", titre);
            offreData.put("description", description);
            offreData.put("competencesRequises", competencesRequises);
            offreData.put("niveauRequis", niveauRequis);
            offreData.put("salaire", salaire);

            offreClient.update(id, offreData);

            ra.addFlashAttribute("message", "L'offre a été mise à jour avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Échec de la modification de l'offre : " + e.getMessage());
        }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * Supprime une offre d'emploi.
     */
    @PostMapping("/offre/supprimer/{id}")
    public String supprimerOffre(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            offreClient.delete(id);
            ra.addFlashAttribute("message", "L'offre a été supprimée avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer l'offre : " + e.getMessage());
        }
        return "redirect:/dashboard-entreprise";
    }

    // =========================================================================
    // CANDIDATURES & CANDIDATS
    // =========================================================================

    /**
     * Permet à un recruteur de modifier l'état d'une candidature reçue.
     */
    @PostMapping("/candidature/statut")
    public String updateCandidatureStatut(@RequestParam Long id, @RequestParam String statut, RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            candidatureClient.updateStatut(id, statut);
            ra.addFlashAttribute("message", "Le statut de la candidature a été mis à jour avec succès en : " + statut);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors du changement de statut.");
        }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * Permet à une entreprise de consulter le profil détaillé d'un candidat ayant postulé.
     */
    @GetMapping("/candidat/profil/{candidatureId}")
    public String voirProfilCandidat(@PathVariable("candidatureId") Long candidatureId, Model model, RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> profilEntreprise = entrepriseClient.getByUserId(sessionService.getUserId());
            Long entId = Long.valueOf(profilEntreprise.get("id").toString());
            List<Map<String, Object>> candidatures = candidatureClient.getByEntreprise(entId);

            Long candidatId = null;
            for (Map<String, Object> c : candidatures) {
                if (c.get("id") != null && Long.valueOf(c.get("id").toString()).equals(candidatureId)) {
                    candidatId = Long.valueOf(c.get("candidatId").toString());
                    break;
                }
            }

            if (candidatId == null) {
                ra.addFlashAttribute("error", "Impossible de retrouver le candidat associé à cette candidature.");
                return "redirect:/dashboard-entreprise";
            }

            Map<String, Object> candidatProfil = candidatClient.getProfil(candidatId);
            model.addAttribute("candidat", candidatProfil != null ? candidatProfil : new HashMap<>());
            model.addAttribute("profil", profilEntreprise != null ? profilEntreprise : new HashMap<>());

            return "voir-candidat";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors du chargement du profil candidat.");
            return "redirect:/dashboard-entreprise";
        }
    }

    /**
     * Affiche le tableau de bord principal de l'Espace Candidat.
     */
    @GetMapping("/dashboard-candidat")
    public String dashboardCandidat(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isCandidat()) return "redirect:/login";

        Long userId = sessionService.getUserId();
        model.addAttribute("profil", new HashMap<>());
        model.addAttribute("offres", new ArrayList<>());
        model.addAttribute("candidatures", new ArrayList<>());

        try {
            Map<String, Object> profilData = candidatClient.getProfil(userId);
            if (profilData != null) model.addAttribute("profil", profilData);
        } catch (Exception e) {
            Map<String, Object> defaultProfil = new HashMap<>();
            defaultProfil.put("prenom", "Ines");
            defaultProfil.put("nom", "Jaffel Charfi");
            model.addAttribute("profil", defaultProfil);
        }

        try {
            Object reponseOffres = offreClient.getAllActive();
            List<Map<String, Object>> toutesLesOffres = null;

            if (reponseOffres instanceof List) {
                toutesLesOffres = (List<Map<String, Object>>) reponseOffres;
            } else if (reponseOffres instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) reponseOffres;
                if (bodyMap.containsKey("content")) {
                    toutesLesOffres = (List<Map<String, Object>>) bodyMap.get("content");
                }
            }

            List<Map<String, Object>> offresFiltrees = new ArrayList<>();

            if (toutesLesOffres != null && !toutesLesOffres.isEmpty()) {
                for (Map<String, Object> o : toutesLesOffres) {
                    if (o.get("id") == null) continue;
                    Long offreId = Long.valueOf(o.get("id").toString());

                    try {
                        int score = candidatureClient.getScore(userId, offreId);
                        if (score >= 50) {
                            o.put("scoreMatching", score);
                            offresFiltrees.add(o);
                        }
                    } catch (Exception feignEx) {
                        System.err.println("ÉCHEC du calcul du matching pour l'offre ID " + offreId);
                    }
                }
            }
            model.addAttribute("offres", offresFiltrees);

        } catch (Exception e) {
            System.err.println("Erreur générale lors de la récupération des offres.");
        }

        try {
            List<?> candidaturesData = candidatureClient.getByCandidat(userId);
            if (candidaturesData != null) model.addAttribute("candidatures", candidaturesData);
        } catch (Exception e) {
            System.err.println("Erreur candidatures: " + e.getMessage());
        }

        return "dashboard-candidat";
    }

    /**
     * Accède à la page de gestion et de modification du profil du candidat.
     */
    @GetMapping("/profil")
    public String profilPage(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";
        try {
            Map<String, Object> profil = candidatClient.getProfil(sessionService.getUserId());
            model.addAttribute("profil", profil != null ? profil : new HashMap<>());
        } catch (Exception e) {
            model.addAttribute("profil", new HashMap<>());
        }
        return "profil-candidat";
    }

    /**
     * Traite la mise à jour des informations du profil candidat.
     */
    @PostMapping("/profil/update")
    public String handleProfilUpdate(@RequestParam String prenom, @RequestParam String nom,
                                     @RequestParam(required = false) String telephone, @RequestParam(required = false) String adresse,
                                     @RequestParam(required = false) String bio, @RequestParam(required = false) String competences,
                                     @RequestParam(required = false) String linkedinUrl, @RequestParam(required = false) String portfolioUrl,
                                     @RequestParam(required = false) String niveauScolaire, @RequestParam(required = false) MultipartFile cv,
                                     @RequestParam(required = false) MultipartFile photo, RedirectAttributes ra) {
        try {
            candidatClient.updateProfil(sessionService.getUserId(), prenom, nom, telephone, adresse, bio, competences, linkedinUrl, portfolioUrl, niveauScolaire, cv, photo);
            ra.addFlashAttribute("message", "Profil mis à jour avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/dashboard-candidat";
    }

    /**
     * Permet à un candidat de postuler formellement à une offre d'emploi.
     */
    @PostMapping("/postuler")
    public String postuler(@RequestParam Long offreId, RedirectAttributes ra) {
        try {
            candidatureClient.postuler(sessionService.getUserId(), offreId);
            ra.addFlashAttribute("message", "Candidature envoyée !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur.");
        }
        return "redirect:/dashboard-candidat";
    }

    /**
     * Page de consultation brute de l'ensemble des offres actives du système.
     */
    @GetMapping("/offre")
    public String listOffres(Model model) {
        if (!sessionService.isAuthenticated()) return "redirect:/login";
        try {
            model.addAttribute("offres", offreClient.getAllActive());
        } catch (Exception e) {
            model.addAttribute("offres", new ArrayList<>());
        }
        return "offre";
    }

    // =========================================================================
    // PLANIFICATION ENTRETIEN (PROXIED TO BACKEND)
    // =========================================================================

    /**
     * Transmet la planification de l'entretien vers le microservice Backend via Feign.
     */
    @PostMapping("/entreprise/entretiens/planifier")
    public String planifierEntretien(
            @RequestParam("candidatureId") Long candidatureId,
            @RequestParam("date") String dateStr,
            @RequestParam("lieu") String lieu,
            @RequestParam("notes") String notes,
            RedirectAttributes redirectAttributes) {

        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";

        try {
            candidatureClient.planifierEntretien(candidatureId, dateStr, lieu, notes);
            redirectAttributes.addFlashAttribute("message", "L'entretien a été planifié avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la planification de l'entretien via le service backend.");
        }

        return "redirect:/dashboard-entreprise";
    }

    // =========================================================================
    // API ENDPOINTS (PROXIES POUR IMAGES ET AVATARS BINAIRES)
    // =========================================================================

    @GetMapping("/api/candidats/avatar/{userId}")
    @ResponseBody
    public ResponseEntity<byte[]> proxyAvatar(@PathVariable Long userId) {
        try {
            return candidatClient.getAvatar(userId);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/entreprises/{id}/logo")
    @ResponseBody
    public ResponseEntity<byte[]> proxyLogo(@PathVariable("id") Long entrepriseId) {
        try {
            return entrepriseClient.getLogo(entrepriseId);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/candidats/download/cv/{userId}")
    public ResponseEntity<byte[]> proxyDownloadCv(@PathVariable Long userId) {
        try {
            ResponseEntity<byte[]> response = candidatClient.downloadCV(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String contentDisposition = response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION)
                        ? response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                        : "attachment; filename=\"cv_" + userId + ".pdf\"";

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(response.getBody());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}