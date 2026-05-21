package com.ines.skillmatch_entreprise_service.dto;

import lombok.Data;

@Data
public class EntrepriseDTO {
    private String nomEntreprise;
    private String secteur;
    private String description;
    private String siteWeb;
    private String telephone;
    private String contactEmail;
}