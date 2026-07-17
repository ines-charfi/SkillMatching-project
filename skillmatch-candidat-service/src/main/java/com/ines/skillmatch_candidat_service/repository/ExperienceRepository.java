package com.ines.skillmatch_candidat_service.repository;

import com.ines.skillmatch_candidat_service.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    /**
     * Retrieves all experiences associated with a specific candidate,
     * identified by the candidate's internal database ID (the primary key
     * of the Candidat entity).
     *
     * This method is useful when you already have the Candidat ID
     * from a previous query or from the entity itself.
     *
     * Spring Data JPA derives the query from the method name:
     * "findByCandidatId" → WHERE candidat.id = ?
     *
     * @param candidatId the internal ID of the candidate (Candidat.id)
     * @return a list of Experience entities belonging to that candidate
     */
    List<Experience> findByCandidatId(Long candidatId);

    /**
     * Retrieves all experiences associated with a candidate using the
     * authentication user ID (userId), which is the foreign key linking
     * to the authentication/account service.
     *
     * This method is more efficient than {@link #findByCandidatId(Long)}
     * when you only have the userId available (e.g., from the security
     * context), because it avoids an extra query to fetch the Candidat
     * entity first. It directly queries the experiences table using the
     * userId field from the associated Candidat.
     *
     * Spring Data JPA derives the query from the method name:
     * "findByCandidatUserId" → WHERE candidat.userId = ?
     * (It navigates the relationship: Experience → Candidat → userId)
     *
     * @param userId the authentication user ID
     * @return a list of Experience entities for that user
     */
    List<Experience> findByCandidatUserId(Long userId);

    /**
     * Deletes all experience records associated with a specific candidate,
     * identified by the candidate's internal database ID.
     *
     * This is a bulk delete operation. It is often used when deleting a
     * candidate profile entirely, ensuring all related experiences are
     * removed without having to delete them one by one.
     *
     * The method name follows Spring Data's derived query convention:
     * "deleteByCandidatId" → DELETE FROM experience WHERE candidat_id = ?
     *
     * @param candidatId the internal ID of the candidate (Candidat.id)
     */
    void deleteByCandidatId(Long candidatId);
}