package com.ines.skillmatch_candidat_service.repository;
import com.ines.skillmatch_candidat_service.model.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    /**
     * Retrieves a candidate profile by the associated user ID.
     *
     * This is a derived query method — Spring Data JPA automatically
     * generates the SQL/JPQL based on the method name.
     *
     * @param userId the unique identifier of the user in the authentication service
     * @return an Optional containing the Candidat if found, or empty otherwise
     */
    Optional<Candidat> findByUserId(Long userId);

    /**
     * Searches for candidates whose 'competences' (skills) field contains
     * the given skill substring (case-sensitive as per LIKE in JPQL).
     *
     * Uses a custom JPQL query with the LIKE operator and wildcard '%'
     * on both sides to perform a partial match.
     *
     * Example: findByCompetence("Java") will match "Java, Spring", "Advanced Java", etc.
     *
     * @param skill the skill keyword to search for (e.g., "Java", "Python", "Docker")
     * @return a list of Candidat entities whose competences contain the given skill
     */
    @Query("SELECT c FROM Candidat c WHERE c.competences LIKE %:skill%")
    List<Candidat> findByCompetence(@Param("skill") String skill);
    /**
     * Retrieves all candidates that have a specific validation status.
     *
     * This is useful for administrative features, such as listing all
     * candidates waiting for approval (EN_ATTENTE), or retrieving all
     * validated profiles (VALIDE) for public display.
     *
     * @param statut the validation status to filter by (EN_ATTENTE, VALIDE, or REJETE)
     * @return a list of Candidat entities with the specified validation status
     */
    @Query("SELECT c FROM Candidat c WHERE c.validationStatut = :statut")
    List<Candidat> findByValidationStatut(@Param("statut") Candidat.ValidationStatut statut);
    /**
     * Searches for candidates by their last name (nom) or first name (prenom)
     * with case-insensitive partial matching.
     *
     * This is a derived query method using Spring Data's keyword
     * "ContainingIgnoreCase" — it generates a JPQL query with
     * LOWER() and LIKE to perform case-insensitive contains searches.
     *
     * @param nom  the last name fragment to search for (can be partial)
     * @param prenom the first name fragment to search for (can be partial)
     * @return a list of Candidat entities whose nom or prenom contains the given terms
     */
    List<Candidat> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom);
}