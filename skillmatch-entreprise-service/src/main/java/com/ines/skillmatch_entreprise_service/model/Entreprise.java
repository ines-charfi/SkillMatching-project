package com.ines.skillmatch_entreprise_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entreprises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "nom_entreprise", nullable = false)
    private String nomEntreprise;

    private String secteur;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "site_web")
    private String siteWeb;

    @Column(name = "logo_path")
    private String logoPath;

    private String telephone;

    @Column(name = "contact_email")
    private String contactEmail;
}