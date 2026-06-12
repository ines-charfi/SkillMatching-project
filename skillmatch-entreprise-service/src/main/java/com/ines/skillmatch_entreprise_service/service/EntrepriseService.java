package com.ines.skillmatch_entreprise_service.service;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.repository.EntrepriseRepository;
import com.ines.skillmatch_entreprise_service.service.client.NotificationClient; // 🎯 AJOUT : Import du client de notification
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 🎯 AJOUT : Pour les logs de sécurité
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
@Slf4j // 🎯 AJOUT : Pour logger les exceptions proprement
public class EntrepriseService {

    private final EntrepriseRepository repository;
    private final NotificationClient notificationClient; // 🎯 AJOUT : Injection du client Feign pour l'envoi de notifs

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // 1. INITIALISATION (Appelé par Auth-Service via Feign)
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

    public Entreprise getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
    }

    public Entreprise getByUserId(Long userId) {
        return repository.findByUserId(userId).orElse(new Entreprise());
    }

    // 2. MISE À JOUR AVEC LOGO
    @Transactional
    public Entreprise updateProfil(Long userId, EntrepriseDTO dto, MultipartFile logo) throws IOException {
        Entreprise e = repository.findByUserId(userId)
                .orElse(Entreprise.builder().userId(userId).build());

        e.setNomEntreprise(dto.getNomEntreprise());
        e.setSecteur(dto.getSecteur());
        e.setDescription(dto.getDescription());
        e.setSiteWeb(dto.getSiteWeb());
        e.setTelephone(dto.getTelephone());
        e.setContactEmail(dto.getContactEmail());

        boolean hasNewLogo = false;

        if (logo != null && !logo.isEmpty()) {
            String fileName = saveFile(logo, "logos");
            e.setLogoPath(fileName);
            hasNewLogo = true; // 🎯 On lève un drapeau si un logo a bien été envoyé
        }

        Entreprise savedEntreprise = repository.save(e);

        // 🎯 AJOUT : Si un logo a été ajouté/modifié, on envoie une notification à l'admin
        if (hasNewLogo) {
            try {
                Map<String, Object> notifAdmin = new HashMap<>();
                notifAdmin.put("userIdTarget", 1L); // ID fixe de ton administrateur
                notifAdmin.put("recipientRole", "admin"); // Pour cibler son Dashboard
                notifAdmin.put("type", "new_logo");
                notifAdmin.put("titreNotif", "Nouveau logo à valider 🖼️");
                notifAdmin.put("message", "L'entreprise '" + savedEntreprise.getNomEntreprise() + "' a téléversé un nouveau logo pour son profil.");
                notifAdmin.put("lu", false);

                notificationClient.envoyerNotification(notifAdmin);
                log.info("🚀 Notification de nouveau logo envoyée à l'administrateur avec succès.");
            } catch (Exception ex) {
                // Le try-catch ici protège l'utilisateur : si l'auth-service est indisponible, le profil est quand même enregistré !
                log.error("⚠️ Échec de l'envoi de la notification de logo à l'admin : {}", ex.getMessage());
            }
        }

        return savedEntreprise;
    }

    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path path = Paths.get(uploadDir, subDir);
        if (!Files.exists(path)) Files.createDirectories(path);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}