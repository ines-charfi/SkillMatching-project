package com.ines.skillmatch_candidat_service.repository;

import com.ines.skillmatch_candidat_service.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    // Récupère les expériences par l'ID de la table 'candidats'
    List<Experience> findByCandidatId(Long candidatId);

    // Récupère les expériences directement par l'ID de l'Auth (plus rapide !)
    List<Experience> findByCandidatUserId(Long userId);

    void deleteByCandidatId(Long candidatId);
}