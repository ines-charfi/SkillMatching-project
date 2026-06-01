package com.ines.skillmatch_offres_service.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor // Requis pour que Jackson lise la Map du Frontend sans planter
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