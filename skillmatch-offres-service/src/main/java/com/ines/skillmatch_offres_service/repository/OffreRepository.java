package com.ines.skillmatch_offres_service.repository;

import com.ines.skillmatch_offres_service.model.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// JPA repository for the Offre entity. Provides CRUD operations and custom queries.
@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    // Finds all offers posted by a specific company (active or inactive).
    List<Offre> findByEntrepriseId(Long entrepriseId);

    // Finds only active offers posted by a specific company.
    List<Offre> findByEntrepriseIdAndActiveTrue(Long entrepriseId);

    // Finds all active offers (soft-deleted ones are excluded).
    List<Offre> findByActiveTrue();

    // Searches active offers by keyword in title, description, or required skills (case-insensitive).
    @Query("SELECT o FROM Offre o WHERE o.active = true AND " +
            "(LOWER(o.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.competencesRequises) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Offre> searchOffres(@Param("keyword") String keyword);

    // Counts the total number of offers posted by a specific company.
    long countByEntrepriseId(Long entrepriseId);

    // Retrieves active offers sorted by publication date (newest first), for homepage or sidebar.
    @Query("SELECT o FROM Offre o WHERE o.active = true ORDER BY o.datePublication DESC")
    List<Offre> findLatestOffres();
}