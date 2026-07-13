package com.ines.skillmatch_offres_service.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

// Data Transfer Object for creating or updating a job offer.
// Maps JSON payload from the frontend to this internal structure.
@Data
@AllArgsConstructor
// Required by Jackson for deserialization (e.g., when the frontend sends data as a Map or form).
@NoArgsConstructor
public class OffreDTO {
    private Long entrepriseId;
    private Long userId;
    private String titre;
    private String description;
    private String competencesRequises;
    private String niveauRequis;
    private String typeContrat;
    private String salaire;
    private String ville;
}