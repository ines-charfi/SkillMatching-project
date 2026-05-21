package com.ines.skillmatch_candidature_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entretiens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entretien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidature_id", nullable = false)
    private Long candidatureId;

    @Column(name = "date_entretien", nullable = false)
    private LocalDateTime dateEntretien;

    private String lieu;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.PROGRAMME;

    public enum Statut {
        PROGRAMME, TERMINE, ANNULE
    }
}
