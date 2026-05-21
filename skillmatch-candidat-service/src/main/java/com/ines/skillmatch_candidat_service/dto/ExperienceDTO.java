package com.ines.skillmatch_candidat_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExperienceDTO {
    private String poste;
    private String entrepriseNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String description;
}
