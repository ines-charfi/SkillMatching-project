package com.ines.skillmatch_candidature_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;

    @Column(name = "offre_id", nullable = false)
    private Long offreId;

    @Column(name = "score_matching")
    @Builder.Default
    private Integer scoreMatching = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.EN_ATTENTE;

    @Column(name = "date_postulation")
    @Builder.Default
    private LocalDateTime datePostulation = LocalDateTime.now();

    public enum Statut {
        EN_ATTENTE, ACCEPTE, REFUSE, ENTRETIEN
    }
}