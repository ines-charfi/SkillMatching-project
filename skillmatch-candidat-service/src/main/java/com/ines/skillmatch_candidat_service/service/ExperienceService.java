package com.ines.skillmatch_candidat_service.service;
import com.ines.skillmatch_candidat_service.dto.ExperienceDTO;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.model.Experience;
import com.ines.skillmatch_candidat_service.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final CandidatService candidatService;

    @Transactional
    public Experience addExperience(Long userId, ExperienceDTO dto) {
        Candidat candidat = candidatService.getByUserId(userId);

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

    public List<Experience> getExperiencesByCandidat(Long candidatId) {
        return experienceRepository.findByCandidatId(candidatId);
    }

    public List<Experience> getExperiencesByUserId(Long userId) {
        Candidat candidat = candidatService.getByUserId(userId);
        return experienceRepository.findByCandidatId(candidat.getId());
    }
}
