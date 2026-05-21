package com.ines.skillmatch_entreprise_service.repository;

import com.ines.skillmatch_entreprise_service.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    Optional<Entreprise> findByUserId(Long userId);

    Optional<Entreprise> findByNomEntreprise(String nom);

    boolean existsByUserId(Long userId);
}