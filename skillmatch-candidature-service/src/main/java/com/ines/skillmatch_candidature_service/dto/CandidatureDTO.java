package com.ines.skillmatch_candidature_service.dto;

import com.ines.skillmatch_candidature_service.model.Candidature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for Candidature (Application).
 * Used to transport application data between the backend services and the frontend UI.
 */

//Generates Getters, Setters, toString(), equals(), and hashCode().
@Data
//Implements the Builder Pattern for fluent and flexible object creation.
@Builder
//Generates a mandatory default constructor (required by Hibernate/JPA and JSON parsers).
@NoArgsConstructor
//Generates a constructor with all fields (required by the @Builder mechanism).
@AllArgsConstructor
public class CandidatureDTO {

    // Core Technical Identifiers
    private Long id;              // Unique identifier of the application
    private Long candidatId;      // ID of the applicant (Candidate)
    private Long offreId;         // ID of the job offer (Job Post)

    // Application Metrics and Status
    private Integer scoreMatching;        // Matching percentage/score between candidate skills and job requirements
    private String statut;                // Current status of the application (e.g., PENDING, ACCEPTED, REJECTED)
    private LocalDateTime datePostulation; // Timestamp of when the application was submitted

    /**
     * Enriched fields specifically added to facilitate UI mockup rendering.
     * These prevent extra API calls or DB joins on the frontend by directly providing human-readable names.
     */
    private String candidatNom;   // Full name of the candidate
    private String offreTitre;    // Title of the job offer
    private String entrepriseNom; // Name of the hiring company
}