package com.ines.skillmatch_candidat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for transferring candidate profile data between the client and the server.
 *
 * This DTO is used in the update profile endpoint ({@link com.ines.skillmatch_candidat_service.controller.CandidatController#updateProfil})
 * to collect form data from the client without exposing the internal JPA entity (Candidat) directly.
 *
 * It is intentionally separate from the entity to avoid serialization issues and to allow controlled
 * input validation in the future.
 */
@Data                     // Generates getters, setters, toString(), equals(), and hashCode()
@Builder                 // (CRUCIAL) Enables the builder pattern: CandidatDTO.builder().nom("...").build()
@NoArgsConstructor       // Generates an empty constructor (required by frameworks like Jackson for deserialization)
@AllArgsConstructor      // Generates a constructor with all fields (required by @Builder to work properly)
public class CandidatDTO {

    /**
     * The candidate's last name (family name).
     * This field is required during profile creation/update.
     */
    private String nom;

    /**
     * The candidate's first name (given name).
     * This field is required during profile creation/update.
     */
    private String prenom;

    /**
     * The candidate's phone number.
     * Optional field.
     */
    private String telephone;

    /**
     * The candidate's physical address.
     * Optional field.
     */
    private String adresse;

    /**
     * A short biography or personal description of the candidate.
     * Optional field.
     */
    private String bio;

    /**
     * A comma-separated string (or any string format) listing the candidate's skills/competences.
     * Optional field. Example: "Java, Spring, Docker"
     */
    private String competences;

    /**
     * The URL to the candidate's LinkedIn profile.
     * Optional field.
     */
    private String linkedinUrl;

    /**
     * The URL to the candidate's personal portfolio website or online project showcase.
     * Optional field.
     */
    private String portfolioUrl;

    /**
     * The candidate's highest level of education (e.g., "Bachelor", "Master", "PhD").
     * Optional field.
     */
    private String niveauScolaire;
}