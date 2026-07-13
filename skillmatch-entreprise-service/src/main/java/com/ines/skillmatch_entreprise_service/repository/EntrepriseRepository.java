package com.ines.skillmatch_entreprise_service.repository;

import com.ines.skillmatch_entreprise_service.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JPA repository for the Entreprise entity. Provides CRUD operations and custom queries.
@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    // Finds a company profile by the associated user ID (unique).
    Optional<Entreprise> findByUserId(Long userId);

    // Finds a company by its exact name (useful for duplicate checks or search).
    Optional<Entreprise> findByNomEntreprise(String nom);

    // Checks if a company profile already exists for a given user ID.
    boolean existsByUserId(Long userId);
}