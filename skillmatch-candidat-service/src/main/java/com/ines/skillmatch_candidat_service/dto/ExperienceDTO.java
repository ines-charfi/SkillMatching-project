package com.ines.skillmatch_candidat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder // --- TRÈS IMPORTANT
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    private String poste;
    private String entrepriseNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String description;
}
