package com.ines.skillmatch_entreprise_service.service;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.repository.EntrepriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntrepriseService {

    private final EntrepriseRepository repository;

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

        if (logo != null && !logo.isEmpty()) {
            String fileName = saveFile(logo, "logos");
            e.setLogoPath(fileName);
        }

        return repository.save(e);
    }

    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path path = Paths.get(uploadDir, subDir);
        if (!Files.exists(path)) Files.createDirectories(path);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}