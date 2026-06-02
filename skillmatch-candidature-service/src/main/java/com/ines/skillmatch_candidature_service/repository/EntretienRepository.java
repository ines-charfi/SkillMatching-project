package com.ines.skillmatch_candidature_service.repository;

import com.ines.skillmatch_candidature_service.model.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {

    List<Entretien> findByCandidatureId(Long candidatureId);
    // Permet de compter tous les entretiens dont l'ID de candidature est dans une liste donnée
    long countByCandidatureIdIn(List<Long> candidatureIds);
}
