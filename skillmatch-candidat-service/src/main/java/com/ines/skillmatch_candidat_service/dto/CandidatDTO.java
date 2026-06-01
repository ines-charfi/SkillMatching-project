package com.ines.skillmatch_candidat_service.dto;

// CandidatDTO.java

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // --- TRÈS IMPORTANT
@NoArgsConstructor
@AllArgsConstructor
public class CandidatDTO {
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String bio;
    private String competences;
    private String linkedinUrl;
    private String portfolioUrl;
    private String niveauScolaire;
}
