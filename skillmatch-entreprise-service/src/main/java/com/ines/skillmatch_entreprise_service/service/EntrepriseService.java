package com.ines.skillmatch_entreprise_service.service;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.repository.EntrepriseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;

    public EntrepriseService(EntrepriseRepository entrepriseRepository) {
        this.entrepriseRepository = entrepriseRepository;
    }

    public Entreprise getById(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée avec l'ID: " + id));
    }

    public Entreprise getByUserId(Long userId) {
        return entrepriseRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil entreprise non trouvé pour l'utilisateur: " + userId));
    }

    @Transactional
    public Entreprise createOrUpdate(Long userId, EntrepriseDTO dto) {
        Entreprise entreprise = entrepriseRepository.findByUserId(userId)
                .orElse(Entreprise.builder().userId(userId).build());

        entreprise.setNomEntreprise(dto.getNomEntreprise());
        entreprise.setSecteur(dto.getSecteur());
        entreprise.setDescription(dto.getDescription());
        entreprise.setSiteWeb(dto.getSiteWeb());
        entreprise.setTelephone(dto.getTelephone());
        entreprise.setContactEmail(dto.getContactEmail());

        return entrepriseRepository.save(entreprise);
    }

    @Transactional
    public String uploadLogo(Long userId, MultipartFile file) throws IOException {
        Entreprise entreprise = getByUserId(userId);

        Path uploadPath = Paths.get("uploads", "logos");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        entreprise.setLogoPath(filePath.toString());
        entrepriseRepository.save(entreprise);

        return filePath.toString();
    }
}