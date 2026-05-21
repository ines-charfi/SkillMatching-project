package com.ines.skillmatch_candidature_service.repository;

import com.ines.skillmatch_candidature_service.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findByCandidatId(Long candidatId);

    List<Candidature> findByOffreId(Long offreId);

    long countByOffreId(Long offreId);

    long countByCandidatId(Long candidatId);

    boolean existsByCandidatIdAndOffreId(Long candidatId, Long offreId);
}
