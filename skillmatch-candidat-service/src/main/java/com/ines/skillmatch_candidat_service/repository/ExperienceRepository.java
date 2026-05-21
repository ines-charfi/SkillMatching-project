package com.ines.skillmatch_candidat_service.repository;

import com.ines.skillmatch_candidat_service.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByCandidatId(Long candidatId);
    void deleteByCandidatId(Long candidatId);
}
