package com.ines.skillmatch_offres_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OffreDTO {
    private Long entrepriseId;
    private String titre;
    private String description;
    private String competencesRequises;
    private String niveauRequis;
    private String typeContrat;
    private String salaire;
    private String ville;
}
