package com.ines.skillmatch_candidature_service.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class CandidatureDTOTest {

    @Test
    void shouldCreateCandidatureDTOUsingBuilder() {
        LocalDateTime now = LocalDateTime.now();
        CandidatureDTO dto = CandidatureDTO.builder()
                .id(1L)
                .candidatId(10L)
                .offreId(20L)
                .scoreMatching(85)
                .statut("EN_ATTENTE")
                .datePostulation(now)
                .candidatNom("Jean Dupont")
                .offreTitre("Développeur Java")
                .entrepriseNom("TechCorp")
                .build();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCandidatId()).isEqualTo(10L);
        assertThat(dto.getOffreId()).isEqualTo(20L);
        assertThat(dto.getScoreMatching()).isEqualTo(85);
        assertThat(dto.getStatut()).isEqualTo("EN_ATTENTE");
        assertThat(dto.getDatePostulation()).isEqualTo(now);
        assertThat(dto.getCandidatNom()).isEqualTo("Jean Dupont");
        assertThat(dto.getOffreTitre()).isEqualTo("Développeur Java");
        assertThat(dto.getEntrepriseNom()).isEqualTo("TechCorp");
    }

    @Test
    void shouldTestSettersAndGetters() {
        CandidatureDTO dto = new CandidatureDTO();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(2L);
        dto.setCandidatId(11L);
        dto.setOffreId(21L);
        dto.setScoreMatching(90);
        dto.setStatut("ACCEPTE");
        dto.setDatePostulation(now);
        dto.setCandidatNom("Marie Martin");
        dto.setOffreTitre("Chef de projet");
        dto.setEntrepriseNom("Innovate SA");

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getCandidatId()).isEqualTo(11L);
        assertThat(dto.getOffreId()).isEqualTo(21L);
        assertThat(dto.getScoreMatching()).isEqualTo(90);
        assertThat(dto.getStatut()).isEqualTo("ACCEPTE");
        assertThat(dto.getDatePostulation()).isEqualTo(now);
        assertThat(dto.getCandidatNom()).isEqualTo("Marie Martin");
        assertThat(dto.getOffreTitre()).isEqualTo("Chef de projet");
        assertThat(dto.getEntrepriseNom()).isEqualTo("Innovate SA");
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        CandidatureDTO d1 = CandidatureDTO.builder().id(1L).candidatId(10L).statut("EN_ATTENTE").datePostulation(now).build();
        CandidatureDTO d2 = CandidatureDTO.builder().id(1L).candidatId(10L).statut("EN_ATTENTE").datePostulation(now).build();
        CandidatureDTO d3 = CandidatureDTO.builder().id(2L).candidatId(11L).statut("ACCEPTE").datePostulation(now).build();

        assertThat(d1).isEqualTo(d2);
        assertThat(d1).isNotEqualTo(d3);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        assertThat(d1.hashCode()).isNotEqualTo(d3.hashCode());
    }

    @Test
    void shouldTestToString() {
        CandidatureDTO dto = CandidatureDTO.builder().id(1L).candidatId(10L).offreId(20L).build();
        assertThat(dto.toString()).contains("id=1", "candidatId=10", "offreId=20");
    }

    @Test
    void shouldTestAllArgsConstructorAndNoArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        CandidatureDTO dto = new CandidatureDTO(1L, 10L, 20L, 85, "EN_ATTENTE", now, "Jean", "Dev", "Corp");
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCandidatId()).isEqualTo(10L);
        assertThat(dto.getOffreId()).isEqualTo(20L);
        assertThat(dto.getScoreMatching()).isEqualTo(85);
        assertThat(dto.getStatut()).isEqualTo("EN_ATTENTE");
        assertThat(dto.getDatePostulation()).isEqualTo(now);
        assertThat(dto.getCandidatNom()).isEqualTo("Jean");
        assertThat(dto.getOffreTitre()).isEqualTo("Dev");
        assertThat(dto.getEntrepriseNom()).isEqualTo("Corp");
    }
}