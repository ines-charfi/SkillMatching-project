package com.ines.skillmatch_candidature_service.dto;
import com.ines.skillmatch_candidature_service.model.Candidature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CandidatureDTO {
    private Long id;
    private Long candidatId;
    private Long offreId;
    private Integer scoreMatching;
    private String statut;
    private LocalDateTime datePostulation;

    // Champs enrichis pour la maquette
    private String candidatNom;
    private String offreTitre;
    private String entrepriseNom;
}
