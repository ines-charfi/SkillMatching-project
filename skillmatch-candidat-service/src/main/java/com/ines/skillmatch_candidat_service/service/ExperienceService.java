package com.ines.skillmatch_candidat_service.service;

import com.ines.skillmatch_candidat_service.dto.ExperienceDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.model.Experience;
import com.ines.skillmatch_candidat_service.repository.ExperienceRepository;
import com.ines.skillmatch_candidat_service.repository.CandidatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final CandidatRepository candidatRepository;

    @Transactional
    public Experience addExperience(Long userId, ExperienceDTO dto) {
        // On vérifie que le candidat existe vraiment en base
        Candidat candidat = candidatRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Veuillez d'abord créer votre profil (Nom, Prénom) avant d'ajouter des expériences."));

        Experience experience = Experience.builder()
                .candidat(candidat)
                .poste(dto.getPoste())
                .entrepriseNom(dto.getEntrepriseNom())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .description(dto.getDescription())
                .build();

        return experienceRepository.save(experience);
    }

    @Transactional
    public Experience updateExperience(Long id, ExperienceDTO dto) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expérience non trouvée"));

        experience.setPoste(dto.getPoste());
        experience.setEntrepriseNom(dto.getEntrepriseNom());
        experience.setDateDebut(dto.getDateDebut());
        experience.setDateFin(dto.getDateFin());
        experience.setDescription(dto.getDescription());

        return experienceRepository.save(experience);
    }

    @Transactional
    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    // Utilisation de la méthode optimisée du repository
    public List<Experience> getExperiencesByUserId(Long userId) {
        return experienceRepository.findByCandidatUserId(userId);
    }

    public List<Experience> getExperiencesByCandidat(Long candidatId) {
        return experienceRepository.findByCandidatId(candidatId);
    }
}