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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller managing User Dashboards for both Candidates and Recruiters.
 * It coordinates profile management, job offers, applications, and notifications.
 */
@Controller
public class DashboardController {

    private final CandidatClient candidatClient;
    private final EntrepriseClient entrepriseClient;
    private final OffreClient offreClient;
    private final CandidatureClient candidatureClient;
    private final NotificationClient notificationClient;
    private final SessionService sessionService;

    public DashboardController(CandidatClient candidatClient,
                               EntrepriseClient entrepriseClient,
                               OffreClient offreClient,
                               CandidatureClient candidatureClient,
                               NotificationClient notificationClient,
                               SessionService sessionService) {
        this.candidatClient = candidatClient;
        this.entrepriseClient = entrepriseClient;
        this.offreClient = offreClient;
        this.candidatureClient = candidatureClient;
        this.notificationClient = notificationClient;
        this.sessionService = sessionService;
    }

    /**
     * ENDPOINT: GET /dashboard
     * FUNCTION: Central routing point.
     * LOGIC: Checks authentication and redirects users to their specific dashboard based on their security role.
     */
    @GetMapping("/dashboard")
    public String genericDashboard() {
        if (!sessionService.isAuthenticated()) return "redirect:/login";
        return "redirect:" + sessionService.getRedirectUrlByRole();
    }

    // =========================================================================
    // CANDIDATE SPACE
    // =========================================================================

    /**
     * ENDPOINT: GET /dashboard-candidat
     * FUNCTION: Loads the candidate's personal area.
     * LOGIC:
     * 1. Fetches candidate profile and notifications.
     * 2. Retrieves all active job offers.
     * 3. Calculates a matching score for each offer using the Matching service.
     * 4. Filters offers to show only those with a score >= 50%.
     * 5. Displays the history of submitted applications.
     */
    @GetMapping("/dashboard-candidat")
    public String dashboardCandidat(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isCandidat()) return "redirect:/login";

        Long userId = sessionService.getUserId();
        System.out.println("Chargement Dashboard Candidat pour l'utilisateur ID : " + userId);

        model.addAttribute("profil", new HashMap<>());
        model.addAttribute("offres", new ArrayList<>());
        model.addAttribute("candidatures", new ArrayList<>());

        try {
            model.addAttribute("notifCount", notificationClient.countNonLues(userId, "candidate"));
            model.addAttribute("notifications", notificationClient.getNotifications(userId, "candidate"));
        } catch (Exception e) {
            System.err.println("⚠️ Échec notifications candidat : " + e.getMessage());
            model.addAttribute("notifCount", 0);
            model.addAttribute("notifications", new ArrayList<>());
        }

        try {
            Map<String, Object> profil = candidatClient.getProfil(userId);
            System.out.println(">>>> PROFIL BRUT RECU : " + profil);
            model.addAttribute("profil", profil);
        } catch (Exception e) {
            System.err.println("❌ Erreur Feign getProfil : " + e.getMessage());
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

                            if (o.containsKey("entrepriseNom")) {
                                o.put("entrepriseNom", o.get("entrepriseNom"));
                            } else if (o.containsKey("nomEntreprise")) {
                                o.put("entrepriseNom", o.get("nomEntreprise"));
                            } else {
                                o.put("entrepriseNom", "Société anonyme (ID: " + o.get("entrepriseId") + ")");
                            }

                            offresFiltrees.add(o);
                        }
                    } catch (Exception feignEx) {
                        System.err.println(" Calcul de matching indisponible pour l'offre ID " + offreId);
                    }
                }
            }
            model.addAttribute("offres", offresFiltrees);
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération des offres : " + e.getMessage());
        }

        try {
            List<?> candidaturesData = candidatureClient.getByCandidat(userId);
            if (candidaturesData != null) model.addAttribute("candidatures", candidaturesData);
        } catch (Exception e) {
            System.err.println("❌ Erreur candidatures : " + e.getMessage());
        }

        return "dashboard-candidat";
    }

    /**
     * ENDPOINT: GET /profil
     * FUNCTION: Renders the profile settings page for a candidate.
     */
    @GetMapping("/profil")
    public String profilPage(Model model) {
        if (!sessionService.isAuthenticated() || sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> profil = candidatClient.getProfil(sessionService.getUserId());
            model.addAttribute("profil", profil != null ? profil : new HashMap<>());
        } catch (Exception e) {
            System.err.println("❌ Impossible de charger la page de configuration profil : " + e.getMessage());
            model.addAttribute("profil", new HashMap<>());
        }
        return "profil-candidat";
    }

    /**
     * ENDPOINT: POST /profil/update
     * FUNCTION: Processes candidate profile updates including file uploads.
     * LOGIC: Sanitizes null inputs to empty strings for Feign compatibility and sends multi-part data (CV, Photo).
     */
    @PostMapping("/profil/update")
    public String handleProfilUpdate(@RequestParam("prenom") String prenom,
                                     @RequestParam("nom") String nom,
                                     @RequestParam(value = "telephone", required = false) String telephone,
                                     @RequestParam(value = "adresse", required = false) String adresse,
                                     @RequestParam(value = "bio", required = false) String bio,
                                     @RequestParam(value = "competences", required = false) String competences,
                                     @RequestParam(value = "linkedinUrl", required = false) String linkedinUrl,
                                     @RequestParam(value = "portfolioUrl", required = false) String portfolioUrl,
                                     @RequestParam(value = "niveauScolaire", required = false) String niveauScolaire,
                                     @RequestParam(value = "cv", required = false) MultipartFile cv,
                                     @RequestParam(value = "photo", required = false) MultipartFile photo,
                                     RedirectAttributes ra) {
        try {
            String telParam = (telephone != null) ? telephone : "";
            String adrParam = (adresse != null) ? adresse : "";
            String bioParam = (bio != null) ? bio : "";
            String compParam = (competences != null) ? competences : "";
            String linkParam = (linkedinUrl != null) ? linkedinUrl : "";
            String portParam = (portfolioUrl != null) ? portfolioUrl : "";
            String nivParam = (niveauScolaire != null) ? niveauScolaire : "";

            candidatClient.updateProfil(
                    sessionService.getUserId(),
                    prenom,
                    nom,
                    telParam,
                    adrParam,
                    bioParam,
                    compParam,
                    linkParam,
                    portParam,
                    nivParam,
                    cv,
                    photo
            );

            ra.addFlashAttribute("message", "Votre espace profil a été mis à jour avec succès !");
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC DU SOUFFLAGE DE DONNÉES FRONTEND : " + e.getMessage());
            ra.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/dashboard-candidat";
    }

    /**
     * ENDPOINT: POST /postuler
     * FUNCTION: Submits a new job application for the current user.
     */
    @PostMapping("/postuler")
    public String postuler(@RequestParam Long offreId, RedirectAttributes ra) {
        try {
            candidatureClient.postuler(sessionService.getUserId(), offreId);
            ra.addFlashAttribute("message", "Votre candidature a été transmise avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Échec de l'envoi de la candidature.");
        }
        return "redirect:/dashboard-candidat";
    }

    /**
     * ENDPOINT: GET /api/candidats/avatar/{userId}
     * FUNCTION: Proxies requests to retrieve a candidate's avatar from the storage service.
     */
    @GetMapping("/api/candidats/avatar/{userId}")
    @ResponseBody
    public ResponseEntity<byte[]> proxyAvatar(@PathVariable Long userId) {
        try {
            return candidatClient.getAvatar(userId);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ENDPOINT: GET /api/candidats/download/cv/{userId}
     * FUNCTION: Proxies the download of a CV file, enforcing PDF media type.
     */
    @GetMapping("/api/candidats/download/cv/{userId}")
    public ResponseEntity<byte[]> proxyDownloadCv(@PathVariable Long userId) {
        try {
            ResponseEntity<byte[]> response = candidatClient.downloadCV(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv_" + userId + ".pdf\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(response.getBody());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur Proxy CV : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================================================================
    // RECRUITER / ENTERPRISE SPACE
    // =========================================================================

    /**
     * ENDPOINT: GET /dashboard-entreprise
     * FUNCTION: Loads the recruiter's management area.
     * LOGIC:
     * 1. Fetches company profile and notifications.
     * 2. Retrieves company-specific statistics and published offers.
     * 3. Fetches all received applications and enriches them with Candidate's full name and Matching scores.
     */
    @GetMapping("/dashboard-entreprise")
    public String dashboardEntreprise(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        Long userId = sessionService.getUserId();

        model.addAttribute("profil", new HashMap<>());
        model.addAttribute("offres", new ArrayList<>());
        model.addAttribute("stats", new HashMap<>());
        model.addAttribute("candidaturesRecues", new ArrayList<>());
        model.addAttribute("notifCount", 0);
        model.addAttribute("notifications", new ArrayList<>());

        try {
            Map<String, Object> profil = entrepriseClient.getByUserId(userId);
            if (profil != null) {
                model.addAttribute("profil", profil);

                try {
                    model.addAttribute("notifCount", notificationClient.countNonLues(userId, "recruiter"));
                    model.addAttribute("notifications", notificationClient.getNotifications(userId, "recruiter"));
                } catch (Exception e) {
                    System.err.println("⚠️ Échec récupération notifications entreprise : " + e.getMessage());
                }

                if (profil.get("id") != null) {
                    Long entId = Long.valueOf(profil.get("id").toString());

                    try { model.addAttribute("offres", offreClient.getByEntreprise(entId)); } catch (Exception e) {}
                    try { model.addAttribute("stats", candidatureClient.getStatsEntreprise(entId)); } catch (Exception e) {}

                    try {
                        List<Map<String, Object>> candidatures = candidatureClient.getByEntreprise(entId);
                        if (candidatures != null) {
                            for (Map<String, Object> c : candidatures) {
                                if (c.get("candidatId") != null) {
                                    Long candidatId = Long.valueOf(c.get("candidatId").toString());

                                    try {
                                        Map<String, Object> profilCandidat = candidatClient.getProfil(candidatId);
                                        if (profilCandidat != null) {
                                            String prenom = profilCandidat.get("prenom") != null ? profilCandidat.get("prenom").toString() : "";
                                            String nom = profilCandidat.get("nom") != null ? profilCandidat.get("nom").toString() : "";
                                            c.put("candidatNomComplet", prenom + " " + nom);
                                        } else {
                                            c.put("candidatNomComplet", "Candidat N°" + candidatId);
                                        }
                                    } catch (Exception e) {
                                        c.put("candidatNomComplet", "Candidat N°" + candidatId);
                                    }

                                    try {
                                        if (c.get("offreId") != null) {
                                            Long offreId = Long.valueOf(c.get("offreId").toString());
                                            int score = candidatureClient.getScore(candidatId, offreId);
                                            c.put("scoreMatching", score);
                                        }
                                    } catch (Exception feignEx) {
                                        c.put("scoreMatching", 0);
                                    }
                                }
                            }
                        }
                        model.addAttribute("candidaturesRecues", candidatures);
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du traitement des données recruteur.");
        }
        return "dashboard-entreprise";
    }

    /**
     * ENDPOINT: GET /profil-entreprise
     * FUNCTION: Renders the company profile edit page.
     */
    @GetMapping("/profil-entreprise")
    public String profilEntreprisePage(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try { model.addAttribute("profil", entrepriseClient.getByUserId(sessionService.getUserId())); } catch (Exception e) { model.addAttribute("profil", new HashMap<>()); }
        return "profil-entreprise";
    }

    /**
     * ENDPOINT: POST /profil-entreprise/update
     * FUNCTION: Updates company information including the corporate logo.
     */
    @PostMapping("/profil-entreprise/update")
    public String handleEntrepriseProfilUpdate(@RequestParam String nomEntreprise,
                                               @RequestParam(required = false) String secteur,
                                               @RequestParam(required = false) String description,
                                               @RequestParam(required = false) String contactEmail,
                                               @RequestParam(required = false) String telephone,
                                               @RequestParam(required = false) String siteWeb,
                                               @RequestParam(required = false) String ville,
                                               @RequestParam(required = false) MultipartFile logo, RedirectAttributes ra) {
        try { entrepriseClient.updateProfil(sessionService.getUserId(), nomEntreprise, secteur, description, siteWeb, telephone, contactEmail, logo);
            ra.addFlashAttribute("message", "Profil entreprise mis à jour !"); }
        catch (Exception e) { ra.addFlashAttribute("error", "Échec."); }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * ENDPOINT: GET /offre/nouveau
     * FUNCTION: Displays the form for creating a new job offer.
     */
    @GetMapping("/offre/nouveau")
    public String nouvelleOffreForm(Model model) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try { model.addAttribute("profil", entrepriseClient.getByUserId(sessionService.getUserId())); }
        catch (Exception e) { model.addAttribute("profil", new HashMap<>()); }
        return "creer-offre";
    }

    /**
     * ENDPOINT: POST /offre/creer
     * FUNCTION: Saves a new job offer to the platform.
     * LOGIC: Injects both the internal company ID and the associated User ID for tracking.
     */
    @PostMapping("/offre/creer")
    public String handleOffreCreation(@RequestParam String titre,
                                      @RequestParam String description,
                                      @RequestParam String niveauRequis,
                                      @RequestParam String salaire,
                                      @RequestParam String competencesRequises,
                                      RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            Map<String, Object> profil = entrepriseClient.getByUserId(sessionService.getUserId());
            Long codebaseId = (profil != null && profil.get("id") != null) ? Long.valueOf(profil.get("id").toString()) : null;

            Map<String, Object> offreData = new HashMap<>();
            offreData.put("entrepriseId", codebaseId);
            offreData.put("userId", sessionService.getUserId());
            offreData.put("titre", titre);
            offreData.put("description", description);
            offreData.put("competencesRequises", competencesRequises);
            offreData.put("niveauRequis", niveauRequis);
            offreData.put("salaire", salaire);
            offreData.put("ville", (profil != null && profil.get("ville") != null) ? profil.get("ville").toString() : "");
            offreData.put("typeContrat", "CDI");

            offreClient.create(offreData);
            ra.addFlashAttribute("message", "Offre d'emploi publiée !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur publication.");
        }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * ENDPOINT: GET /offre/modifier/{id}
     * FUNCTION: Retrieves an existing offer's data for the edit form.
     */
    @GetMapping("/offre/modifier/{id}")
    public String modifierOffreForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        if (!sessionService.isAuthenticated() || !sessionService.isEntreprise()) return "redirect:/login";
        try {
            model.addAttribute("profil", entrepriseClient.getByUserId(sessionService.getUserId()));
            model.addAttribute("offre", (Map<String, Object>)(Object)offreClient.getById(id));
            return "modifier-offre";
        } catch (Exception e) { return "redirect:/dashboard-entreprise"; }
    }

    /**
     * ENDPOINT: POST /offre/update/{id}
     * FUNCTION: Updates an existing job offer's details.
     */
    @PostMapping("/offre/update/{id}")
    public String handleOffreUpdate(@PathVariable("id") Long id,
                                    @RequestParam String titre,
                                    @RequestParam String description,
                                    @RequestParam String niveauRequis,
                                    @RequestParam String salaire,
                                    @RequestParam String competencesRequises, RedirectAttributes ra) {
        try {
            Map<String, Object> offreData = new HashMap<>();
            offreData.put("titre", titre); offreData.put("description", description);
            offreData.put("competencesRequises", competencesRequises);
            offreData.put("niveauRequis", niveauRequis); offreData.put("salaire", salaire);
            offreClient.update(id, offreData);
            ra.addFlashAttribute("message", "Offre modifiée !");
        } catch (Exception e) { ra.addFlashAttribute("error", "Erreur."); }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * ENDPOINT: POST /offre/supprimer/{id}
     * FUNCTION: Permanently removes a job offer from the platform.
     */
    @PostMapping("/offre/supprimer/{id}")
    public String supprimerOffre(@PathVariable("id") Long id, RedirectAttributes ra) {
        try { offreClient.delete(id); ra.addFlashAttribute("message", "Offre supprimée !"); }
        catch (Exception e) { ra.addFlashAttribute("error", "Erreur."); }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * ENDPOINT: POST /candidature/statut
     * FUNCTION: Updates the application workflow status (e.g., ACCEPTED, REJECTED).
     */
    @PostMapping("/candidature/statut")
    public String updateCandidatureStatut(@RequestParam Long id, @RequestParam String statut, RedirectAttributes ra) {
        try { candidatureClient.updateStatut(id, statut); ra.addFlashAttribute("message", "Statut changé !"); }
        catch (Exception e) { ra.addFlashAttribute("error", "Erreur."); }
        return "redirect:/dashboard-entreprise";
    }

    /**
     * ENDPOINT: GET /candidat/profil/{candidatureId}
     * FUNCTION: Displays a full candidate profile to a recruiter based on a received application.
     */
    @GetMapping("/candidat/profil/{candidatureId}")
    public String voirProfilCandidat(@PathVariable("candidatureId") Long candidatureId, Model model, RedirectAttributes ra) {
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
            if (candidatId == null) return "redirect:/dashboard-entreprise";
            model.addAttribute("candidat", candidatClient.getProfil(candidatId));
            model.addAttribute("profil", profilEntreprise);
            return "voir-candidat";
        } catch (Exception e) { return "redirect:/dashboard-entreprise"; }
    }

    /**
     * ENDPOINT: GET /offre
     * FUNCTION: Lists all active job offers for public viewing.
     */
    @GetMapping("/offre")
    public String listOffres(Model model) {
        Set<Long> appliedOfferIds = new HashSet<>();
        List<Map<String, Object>> offres = new ArrayList<>();

        try {
            // 1. Récupérer toutes les offres (commun à tout le monde)
            offres = offreClient.getAllActive();

            // 2. Si c'est un CANDIDAT, on cherche ses IDs d'offres déjà postulées
            if ("CANDIDAT".equals(sessionService.getUserRole())) {
                Long userId = sessionService.getUserId();
                List<Map<String, Object>> mesCandidatures = candidatureClient.getByCandidat(userId);

                if (mesCandidatures != null) {
                    appliedOfferIds = mesCandidatures.stream()
                            .map(c -> Long.valueOf(c.get("offreId").toString()))
                            .collect(Collectors.toSet());
                }
            }
        } catch (Exception e) {
            // En cas d'erreur microservice, on laisse les listes vides (évite le crash)
            System.err.println("Erreur récupération offres/candidatures : " + e.getMessage());
        }

        model.addAttribute("offres", offres);
        model.addAttribute("appliedOfferIds", appliedOfferIds);
        return "offre";
    }

    /**
     * ENDPOINT: GET /api/entreprises/{id}/logo
     * FUNCTION: Proxies the retrieval of a company logo from the backend service.
     */
    @GetMapping("/api/entreprises/{id}/logo")
    @ResponseBody
    public ResponseEntity<byte[]> proxyLogo(@PathVariable("id") Long codebaseId) {
        try { return entrepriseClient.getLogo(codebaseId); } catch (Exception e) { return ResponseEntity.notFound().build(); }
    }

    /**
     * ENDPOINT: POST /entreprise/entretiens/planifier
     * FUNCTION: Creates a scheduled interview entry for a candidate.
     */
    @PostMapping("/entreprise/entretiens/planifier")
    public String planifierEntretien(@RequestParam("candidatureId") Long candidatureId,
                                     @RequestParam("date") String dateStr, @RequestParam("lieu") String lieu,
                                     @RequestParam("notes") String notes, RedirectAttributes ra) {
        try { candidatureClient.planifierEntretien(candidatureId, dateStr, lieu, notes);
            ra.addFlashAttribute("message", "Entretien planifié !");
        }
        catch (Exception e) { ra.addFlashAttribute("error", "Échec."); }
        return "redirect:/dashboard-entreprise";
    }
}