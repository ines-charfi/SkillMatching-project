package com.ines.skillmatch_auth_service.controller;

import com.ines.skillmatch_auth_service.dto.UserDto;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // Injection par constructeur (plus propre que @RequiredArgsConstructor)
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    // ============================================
    // USERS
    // ============================================

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = users.stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        user.setEnabled(!user.getEnabled());
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        user.setRole(User.Role.valueOf(role.toUpperCase()));
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long candidats = allUsers.stream().filter(u -> u.getRole() == User.Role.CANDIDAT).count();
        long entreprises = allUsers.stream().filter(u -> u.getRole() == User.Role.ENTREPRISE).count();
        long admins = allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count();
        long enabled = allUsers.stream().filter(User::getEnabled).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("candidats", candidats);
        stats.put("entreprises", entreprises);
        stats.put("admins", admins);
        stats.put("enabled", enabled);
        stats.put("disabled", totalUsers - enabled);

        return ResponseEntity.ok(stats);
    }

    // ============================================
    // VÉRIFICATION FICHIERS
    // ============================================

    @GetMapping("/fichiers-a-verifier")
    public ResponseEntity<List<Map<String, Object>>> getFichiersAVerifier() {
        List<Map<String, Object>> fichiers = new ArrayList<>();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // Candidats
            if (user.getRole() == User.Role.CANDIDAT) {
                try {
                    Map<String, Object> candidat = restTemplate.getForObject(
                            "http://candidat-service/api/candidats/user/" + user.getId(),
                            Map.class);

                    if (candidat != null) {
                        if (candidat.get("cvPath") != null) {
                            Map<String, Object> f = new HashMap<>();
                            f.put("id", "cv_" + candidat.get("id"));
                            f.put("userId", user.getId());
                            f.put("type", "CV");
                            f.put("nom", candidat.get("cvPath").toString());
                            f.put("candidatNom", candidat.get("prenom") + " " + candidat.get("nom"));
                            f.put("dateUpload", candidat.get("dateCreation"));
                            f.put("statut", "EN_ATTENTE");
                            f.put("url", candidat.get("cvPath").toString());
                            fichiers.add(f);
                        }
                        if (candidat.get("photoPath") != null) {
                            Map<String, Object> f = new HashMap<>();
                            f.put("id", "photo_" + candidat.get("id"));
                            f.put("userId", user.getId());
                            f.put("type", "PHOTO");
                            f.put("nom", candidat.get("photoPath").toString());
                            f.put("candidatNom", candidat.get("prenom") + " " + candidat.get("nom"));
                            f.put("dateUpload", candidat.get("dateCreation"));
                            f.put("statut", "EN_ATTENTE");
                            f.put("url", candidat.get("photoPath").toString());
                            fichiers.add(f);
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Entreprises
            if (user.getRole() == User.Role.ENTREPRISE) {
                try {
                    Map<String, Object> entreprise = restTemplate.getForObject(
                            "http://entreprise-service/api/entreprises/user/" + user.getId(),
                            Map.class);

                    if (entreprise != null && entreprise.get("logoPath") != null) {
                        Map<String, Object> f = new HashMap<>();
                        f.put("id", "logo_" + entreprise.get("id"));
                        f.put("userId", user.getId());
                        f.put("type", "LOGO");
                        f.put("nom", entreprise.get("logoPath").toString());
                        f.put("entrepriseNom", entreprise.get("nomEntreprise"));
                        f.put("dateUpload", entreprise.get("dateCreation"));
                        f.put("statut", "EN_ATTENTE");
                        f.put("url", entreprise.get("logoPath").toString());
                        fichiers.add(f);
                    }
                } catch (Exception ignored) {}
            }
        }

        return ResponseEntity.ok(fichiers);
    }

    @PostMapping("/analyser-contenu")
    public ResponseEntity<Map<String, Object>> analyserContenu(@RequestBody Map<String, String> request) {
        String typeFichier = request.get("type");
        Map<String, Object> resultat = analyserAvecIA(typeFichier);
        return ResponseEntity.ok(resultat);
    }

    @PutMapping("/approuver-fichier/{id}")
    public ResponseEntity<Map<String, String>> approuverFichier(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("message", "Fichier approuvé"));
    }

    @PutMapping("/rejeter-fichier/{id}")
    public ResponseEntity<Map<String, String>> rejeterFichier(@PathVariable String id,
                                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("message", "Fichier rejeté", "motif", body.getOrDefault("motif", "")));
    }

    // ============================================
    // PRIVATE METHODS
    // ============================================

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .provider(user.getProvider())
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