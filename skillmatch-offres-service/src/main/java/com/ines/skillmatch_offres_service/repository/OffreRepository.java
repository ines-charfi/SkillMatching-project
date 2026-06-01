package com.ines.skillmatch_offres_service.repository;

import com.ines.skillmatch_offres_service.model.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    List<Offre> findByEntrepriseId(Long entrepriseId);
    List<Offre> findByEntrepriseIdAndActiveTrue(Long entrepriseId);
    List<Offre> findByActiveTrue();

    @Query("SELECT o FROM Offre o WHERE o.active = true AND " +
            "(LOWER(o.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.competencesRequises) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Offre> searchOffres(@Param("keyword") String keyword);

    long countByEntrepriseId(Long entrepriseId);

    @Query("SELECT o FROM Offre o WHERE o.active = true ORDER BY o.datePublication DESC")
    List<Offre> findLatestOffres();
}