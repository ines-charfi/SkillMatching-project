package com.ines.skillmatch_candidat_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String telephone;
    private String adresse;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(name = "cv_path")
    private String cvPath;

    @Column(name = "niveau_scolaire")
    private String niveauScolaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_statut")
    @Builder.Default
    private ValidationStatut validationStatut = ValidationStatut.EN_ATTENTE;

    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "candidat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Experience> experiences = new ArrayList<>();

    public enum ValidationStatut {
        EN_ATTENTE, VALIDE, REJETE
    }
}