package com.ines.skillmatch_candidature_service.repository;

import com.ines.skillmatch_candidature_service.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Layer for the Application entity.
 * Uses Spring Data JPA to provide standard CRUD operations and custom query methods.
 */
@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    /**
     * Finds all applications submitted by a specific candidate.
     * @param candidatId The unique identifier of the candidate
     * @return A list of matching Candidature objects
     */
    List<Candidature> findByCandidatId(Long candidatId);

    /**
     * Finds all applications associated with a specific job offer.
     * @param offreId The unique identifier of the job offer
     * @return A list of matching Candidature objects
     */
    List<Candidature> findByOffreId(Long offreId);

    /**
     * Counts the total number of candidates who applied for a specific job offer.
     * Useful for company-side recruitment metrics.
     */
    long countByOffreId(Long offreId);

    /**
     * Counts how many job offers a specific candidate has applied for.
     */
    long countByCandidatId(Long candidatId);

    /**
     * Checks if a candidate has already applied to a specific offer.
     * Used to prevent duplicate applications (Business Logic Constraint).
     *
     * @return true if an application already exists, false otherwise
     */
    boolean existsByCandidatIdAndOffreId(Long candidatId, Long offreId);
}