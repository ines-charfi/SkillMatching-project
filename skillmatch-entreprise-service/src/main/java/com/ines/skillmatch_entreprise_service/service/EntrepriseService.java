package com.ines.skillmatch_entreprise_service.service;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.repository.EntrepriseRepository;
import com.ines.skillmatch_entreprise_service.service.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntrepriseService {

    private final EntrepriseRepository repository;
    private final NotificationClient notificationClient; // Feign client for sending notifications to the auth service
    private final EntrepriseRepository entrepriseRepository;

    // Base directory where uploaded files (logos) are stored
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Initializes a new company profile. Called by Auth-Service via Feign during registration.
    @Transactional
    public void initEntreprise(Long userId, String nomEntreprise) {
        if (!repository.existsByUserId(userId)) {
            Entreprise entreprise = Entreprise.builder()
                    .userId(userId)
                    .nomEntreprise(nomEntreprise)
                    .build();
            repository.save(entreprise);
        }
    }

    // Fetches a company by its internal ID.
    public Entreprise getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
    }

    // Fetches a company by the associated user ID. Returns a new empty entity if not found.
    public Entreprise getByUserId(Long userId) {
        return repository.findByUserId(userId).orElse(new Entreprise());
    }

    // Updates the company profile with optional logo upload.
    // If a new logo is uploaded, sends a notification to the admin for validation.
    @Transactional
    public Entreprise updateProfil(Long userId, EntrepriseDTO dto, MultipartFile logo) throws IOException {
        Entreprise e = repository.findByUserId(userId)
                .orElse(Entreprise.builder().userId(userId).build());

        // Update text fields from the DTO
        e.setNomEntreprise(dto.getNomEntreprise());
        e.setSecteur(dto.getSecteur());
        e.setDescription(dto.getDescription());
        e.setSiteWeb(dto.getSiteWeb());
        e.setTelephone(dto.getTelephone());
        e.setContactEmail(dto.getContactEmail());

        boolean hasNewLogo = false;

        // Save the uploaded logo file if present
        if (logo != null && !logo.isEmpty()) {
            String fileName = saveFile(logo, "logos");
            e.setLogoPath(fileName);
            hasNewLogo = true;
        }

        Entreprise savedEntreprise = repository.save(e);

        // Send a notification to the admin if a new logo was uploaded
        if (hasNewLogo) {
            try {
                Map<String, Object> notifAdmin = new HashMap<>();
                notifAdmin.put("userIdTarget", 1L); // Fixed admin user ID
                notifAdmin.put("recipientRole", "admin");
                notifAdmin.put("type", "new_logo");
                notifAdmin.put("titreNotif", "Nouveau logo à valider 🖼️");
                notifAdmin.put("message", "L'entreprise '" + savedEntreprise.getNomEntreprise() + "' a téléversé un nouveau logo pour son profil.");
                notifAdmin.put("lu", false);

                notificationClient.envoyerNotification(notifAdmin);
                log.info("🚀 Notification de nouveau logo envoyée à l'administrateur avec succès.");
            } catch (Exception ex) {
                // The try-catch protects the user: if the auth-service is down,
                // the profile is still saved successfully!
                log.error("⚠️ Échec de l'envoi de la notification de logo à l'admin : {}", ex.getMessage());
            }
        }

        return savedEntreprise;
    }

    // Saves a file to the local filesystem and returns the generated filename.
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path path = Paths.get(uploadDir, subDir);
        if (!Files.exists(path)) Files.createDirectories(path);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    // Fetches a company by its ID, throwing an exception if not found.
    public Entreprise findById(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée avec l'ID : " + id));
    }
}