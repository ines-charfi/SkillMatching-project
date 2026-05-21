package com.ines.skillmatch_offres_service.service;

import com.ines.skillmatch_offres_service.dto.OffreDTO;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.repository.OffreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OffreService {

    private final OffreRepository offreRepository;
    private final RestTemplate restTemplate;

    public OffreService(OffreRepository offreRepository, RestTemplate restTemplate) {
        this.offreRepository = offreRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Offre create(OffreDTO dto) {
        Offre offre = Offre.builder()
                .entrepriseId(dto.getEntrepriseId())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .competencesRequises(dto.getCompetencesRequises())
                .niveauRequis(dto.getNiveauRequis())
                .salaire(dto.getSalaire())
                .active(true)
                .build();
        return offreRepository.save(offre);
    }

    @Transactional
    public Offre update(Long id, OffreDTO dto) {
        Offre offre = getById(id);
        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setCompetencesRequises(dto.getCompetencesRequises());
        offre.setNiveauRequis(dto.getNiveauRequis());
        offre.setSalaire(dto.getSalaire());
        return offreRepository.save(offre);
    }

    @Transactional
    public void delete(Long id) {
        Offre offre = getById(id);
        offre.setActive(false);
        offreRepository.save(offre);
    }

    public Offre getById(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + id));
        enrichOffre(offre);
        return offre;
    }

    public List<Offre> getAllActive() {
        List<Offre> offres = offreRepository.findByActiveTrue();
        offres.forEach(this::enrichOffre);
        return offres;
    }

    public List<Offre> getByEntreprise(Long entrepriseId) {
        List<Offre> offres = offreRepository.findByEntrepriseId(entrepriseId);
        offres.forEach(this::enrichOffre);
        return offres;
    }

    public List<Offre> search(String keyword) {
        return offreRepository.searchOffres(keyword);
    }

    public long countByEntreprise(Long entrepriseId) {
        return offreRepository.countByEntrepriseId(entrepriseId);
    }

    public List<Offre> getLatest() {
        return offreRepository.findLatestOffres();
    }

    private void enrichOffre(Offre offre) {
        try {
            Map entreprise = restTemplate.getForObject(
                    "http://entreprise-service/api/entreprises/" + offre.getEntrepriseId(),
                    Map.class);
            if (entreprise != null) {
                offre.setEntrepriseNom((String) entreprise.get("nomEntreprise"));
                offre.setEntrepriseLogo((String) entreprise.get("logoPath"));
            }
        } catch (Exception e) {
            offre.setEntrepriseNom("Entreprise inconnue");
        }

        try {
            Long count = restTemplate.getForObject(
                    "http://candidature-service/api/candidatures/offre/" + offre.getId() + "/count",
                    Long.class);
            offre.setNombreCandidatures(count != null ? count : 0L);
        } catch (Exception e) {
            offre.setNombreCandidatures(0L);
        }
    }
}