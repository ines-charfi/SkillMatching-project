package com.ines.skillmatch_offres_service.service;

import com.ines.skillmatch_offres_service.service.client.CandidatureClient;
import com.ines.skillmatch_offres_service.service.client.EntrepriseClient;
import com.ines.skillmatch_offres_service.dto.OffreDTO;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor // Génère le constructeur pour injecter les repos et clients
@Slf4j // Pour les logs
public class OffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseClient entrepriseClient; // Client Feign
    private final CandidatureClient candidatureClient; // Client Feign
    @Transactional
    public Offre create(OffreDTO dto) {
        Offre offre = Offre.builder()
                .entrepriseId(dto.getEntrepriseId())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .competencesRequises(dto.getCompetencesRequises())
                .niveauRequis(dto.getNiveauRequis())
                .salaire(dto.getSalaire())
                .active(true) // Uniquement les champs présents dans ton script SQL
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
        offre.setActive(false); // Soft delete pour garder l'historique
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
        // 🎯 FIX : On appelle une méthode qui filtre par entreprise ID ET statut actif
        List<Offre> offres = offreRepository.findByEntrepriseIdAndActiveTrue(entrepriseId);
        offres.forEach(this::enrichOffre);
        return offres;
    }

    public List<Offre> search(String keyword) {
        return offreRepository.searchOffres(keyword);
    }

    /**
     * Méthode d'enrichissement via OpenFeign
     * Remplit les champs @Transient pour le Frontend
     */
    private void enrichOffre(Offre offre) {
        // 1. Récupérer les infos de l'entreprise
        try {
            Map<String, Object> entreprise = entrepriseClient.getEntrepriseByUserId(offre.getEntrepriseId());
            if (entreprise != null) {
                offre.setEntrepriseNom((String) entreprise.get("nomEntreprise"));
                offre.setEntrepriseLogo((String) entreprise.get("logoPath"));
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer l'entreprise pour l'offre {}: {}", offre.getId(), e.getMessage());
            offre.setEntrepriseNom("Entreprise inconnue");
        }

        // 2. Récupérer le nombre de candidatures
        try {
            Long count = candidatureClient.CountByOffreId(offre.getId());
            offre.setNombreCandidatures(count != null ? count : 0L);
        } catch (Exception e) {
            log.warn("Impossible de compter les candidatures pour l'offre {}: {}", offre.getId(), e.getMessage());
            offre.setNombreCandidatures(0L);
        }
    }

    public long countByEntreprise(Long entrepriseId) {
        return offreRepository.countByEntrepriseId(entrepriseId);
    }

    public List<Offre> getLatest() {
        return offreRepository.findLatestOffres();
    }
}