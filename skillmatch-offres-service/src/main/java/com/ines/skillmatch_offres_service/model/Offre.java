package com.ines.skillmatch_offres_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entreprise_id", nullable = false)
    private Long entrepriseId;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "competences_requises", columnDefinition = "TEXT")
    private String competencesRequises;

    @Column(name = "niveau_requis")
    private String niveauRequis;

    private String salaire;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "date_publication")
    @Builder.Default
    private LocalDateTime datePublication = LocalDateTime.now();

    // Champs transients (pas en BDD)
    @Transient
    private String entrepriseNom;

    @Transient
    private String entrepriseLogo;

    @Transient
    private Long nombreCandidatures;
}