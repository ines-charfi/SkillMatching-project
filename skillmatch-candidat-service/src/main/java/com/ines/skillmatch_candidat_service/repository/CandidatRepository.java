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

    Optional<Candidat> findByUserId(Long userId);

    @Query("SELECT c FROM Candidat c WHERE " +
            "LOWER(c.competences) LIKE LOWER(CONCAT('%', :competence, '%'))")
    List<Candidat> findByCompetence(@Param("competence") String competence);

    @Query("SELECT c FROM Candidat c WHERE c.validationStatut = :statut")
    List<Candidat> findByValidationStatut(@Param("statut") Candidat.ValidationStatut statut);

    List<Candidat> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom);
}