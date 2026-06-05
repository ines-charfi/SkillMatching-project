package com.ines.skillmatch_auth_service.controller;

import com.ines.skillmatch_auth_service.dto.UserDto;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import com.ines.skillmatch_auth_service.service.client.CandidatClient;
import com.ines.skillmatch_auth_service.service.client.EntrepriseClient;
import com.ines.skillmatch_auth_service.service.client.OffreClient;
import com.ines.skillmatch_auth_service.service.client.CandidatureClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource; // CORRIGÉ : Bon import pour le flux de fichier
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final CandidatClient candidatClient;
    private final EntrepriseClient entrepriseClient;
    private final OffreClient offreClient;
    private final CandidatureClient candidatureClient;

    // ============================================
    // 1. STATISTIQUES GLOBALES
    // ============================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();

        List<User> allUsers = userRepository.findAll();
        stats.put("totalUsers", allUsers.size());
        stats.put("totalCandidats", allUsers.stream().filter(u -> u.getRole() == User.Role.CANDIDAT).count());
        stats.put("totalEntreprises", allUsers.stream().filter(u -> u.getRole() == User.Role.ENTREPRISE).count());
        stats.put("admins", allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count());

        try {
            stats.put("totalOffres", offreClient.countAllOffres());
            stats.put("totalCandidatures", candidatureClient.countAllCandidatures());
        } catch (Exception e) {
            log.error("Erreur récupération stats distantes: {}", e.getMessage());
            stats.put("totalOffres", 0);
            stats.put("totalCandidatures", 0);
        }

        return ResponseEntity.ok(stats);
    }

    // ============================================
    // 2. GESTION DES UTILISATEURS
    // ============================================
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(this::toDto).toList());
    }

    @GetMapping("/latest-users")
    public ResponseEntity<List<UserDto>> getLatestUsers() {
        return ResponseEntity.ok(userRepository.findTop5ByOrderByDateCreationDesc().stream().map(this::toDto).toList());
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<Void> toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // 3. VÉRIFICATION IA DES FICHIERS & AGGRÉGATION
    // ============================================
    @GetMapping("/fichiers-a-verifier")
    public ResponseEntity<List<Map<String, Object>>> getFichiersAVerifier() {
        List<Map<String, Object>> fichiers = new ArrayList<>();

        // 1. Extraction des CV Candidats
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.CANDIDAT)
                .forEach(user -> {
                    try {
                        Map<String, Object> candidat = candidatClient.getCandidatByUserId(user.getId());
                        if (candidat != null && candidat.get("cvPath") != null) {
                            Map<String, Object> f = new HashMap<>();
                            f.put("id", user.getId());
                            f.put("type", "CV");
                            f.put("nom", candidat.get("cvPath"));
                            f.put("proprietaire", candidat.get("prenom") + " " + candidat.get("nom"));
                            f.put("date", candidat.get("dateCreation"));
                            fichiers.add(f);
                        }
                    } catch (Exception ignored) {}
                });

        // 2. Extraction des Logos Entreprises (Ajouté pour dynamiser ton tableau)
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ENTREPRISE)
                .forEach(user -> {
                    try {
                        Map<String, Object> entreprise = entrepriseClient.getEntrepriseByUserId(user.getId());
                        if (entreprise != null && entreprise.get("logoPath") != null) {
                            Map<String, Object> f = new HashMap<>();
                            f.put("id", user.getId());
                            f.put("type", "LOGO");
                            f.put("nom", entreprise.get("logoPath"));
                            f.put("proprietaire", entreprise.get("nomEntreprise"));
                            f.put("date", entreprise.get("dateCreation"));
                            fichiers.add(f);
                        }
                    } catch (Exception ignored) {}
                });

        return ResponseEntity.ok(fichiers);
    }

    @PostMapping("/analyser-contenu")
    public ResponseEntity<Map<String, Object>> analyser(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(analyserAvecIA(req.get("type")));
    }

    // ============================================
    // 4. MODÉRATION DES OFFRES
    // ============================================
    @GetMapping("/offres")
    public ResponseEntity<List<Map<String, Object>>> getAllOffres() {
        try {
            return ResponseEntity.ok(offreClient.getAllOffres());
        } catch (Exception e) {
            log.error("Erreur récupération des offres: {}", e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @DeleteMapping("/offres/{id}")
    public ResponseEntity<Void> supprimerOffre(@PathVariable Long id) {
        try {
            offreClient.deleteOffre(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur suppression offre: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ============================================
    // 5. TUNNEL DE TÉLÉCHARGEMENT SECURISE
    // ============================================
    @GetMapping("/fichiers/download-cv/{candidatId}")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long candidatId) {
        try {
            Map<String, Object> candidat = candidatClient.getCandidatByUserId(candidatId);
            String fileName = (String) candidat.get("cvPath");

            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            RestTemplate restTemplate = new RestTemplate();
            String fileUrl = "http://candidat-service:8082/api/candidats/files/" + fileName;
            byte[] fileBytes = restTemplate.getForObject(fileUrl, byte[].class);

            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du CV: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/fichiers/download-logo/{entrepriseId}")
    public ResponseEntity<Resource> downloadLogo(@PathVariable Long entrepriseId) {
        try {
            Map<String, Object> entreprise = entrepriseClient.getEntrepriseByUserId(entrepriseId);
            String fileName = (String) entreprise.get("logoPath");

            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            RestTemplate restTemplate = new RestTemplate();
            String fileUrl = "http://entreprise-service:8083/api/entreprises/files/" + fileName;
            byte[] fileBytes = restTemplate.getForObject(fileUrl, byte[].class);

            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du logo: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // --- UTILS ---
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .dateCreation(user.getDateCreation())
                .build();
    }

    /**
     * Simulation d'analyse IA
     */
    private Map<String, Object> analyserAvecIA(String typeFichier) {
        Map<String, Object> analyse = new HashMap<>();
        Random random = new Random();

        switch (typeFichier.toUpperCase()) {
            case "CV" -> {
                analyse.put("type", "CV");
                analyse.put("contientNom", true);
                analyse.put("contientEmail", true);
                analyse.put("contientTelephone", random.nextBoolean());
                analyse.put("formatValide", true);
                analyse.put("tailleKo", random.nextInt(500) + 50);
                analyse.put("scoreConformite", random.nextInt(30) + 70);
                analyse.put("problemes", random.nextBoolean() ?
                        List.of("Contient des informations personnelles excessives") : List.of());
            }
            case "PHOTO" -> {
                analyse.put("type", "PHOTO");
                analyse.put("visageDetecte", true);
                analyse.put("qualiteImage", random.nextInt(40) + 60 + "%");
                analyse.put("fondApproprie", random.nextBoolean());
                analyse.put("tailleKo", random.nextInt(200) + 20);
                analyse.put("scoreConformite", random.nextInt(20) + 80);
                analyse.put("problemes", random.nextInt(10) < 2 ?
                        List.of("Photo de mauvaise qualité", "Fond non professionnel") : List.of());
            }
            case "LOGO" -> {
                analyse.put("type", "LOGO");
                analyse.put("texteLisible", true);
                analyse.put("pasDeContenuOffensant", true);
                analyse.put("formatValide", true);
                analyse.put("tailleKo", random.nextInt(100) + 10);
                analyse.put("scoreConformite", random.nextInt(15) + 85);
                analyse.put("problemes", random.nextInt(10) < 1 ?
                        List.of("Logo potentiellement offensant détecté") : List.of());
            }
        }

        int score = (int) analyse.getOrDefault("scoreConformite", 0);
        analyse.put("dateAnalyse", LocalDateTime.now().toString());
        analyse.put("recommendation", score >= 70 ? "APPROUVER" : "REJETER");

        return analyse;
    }
}