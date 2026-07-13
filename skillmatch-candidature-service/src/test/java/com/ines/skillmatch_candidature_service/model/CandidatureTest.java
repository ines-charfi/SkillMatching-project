package com.ines.skillmatch_candidature_service.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class CandidatureTest {

    @Test
    void shouldCreateCandidatureUsingBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Candidature candidature = Candidature.builder()
                .id(1L)
                .candidatId(10L)
                .offreId(20L)
                .scoreMatching(85)
                .statut(Candidature.Statut.EN_ATTENTE)
                .datePostulation(now)
                .build();

        assertThat(candidature.getId()).isEqualTo(1L);
        assertThat(candidature.getCandidatId()).isEqualTo(10L);
        assertThat(candidature.getOffreId()).isEqualTo(20L);
        assertThat(candidature.getScoreMatching()).isEqualTo(85);
        assertThat(candidature.getStatut()).isEqualTo(Candidature.Statut.EN_ATTENTE);
        assertThat(candidature.getDatePostulation()).isEqualTo(now);
    }

    @Test
    void shouldUseDefaultValues() {
        Candidature candidature = new Candidature();
        assertThat(candidature.getScoreMatching()).isEqualTo(0);
        assertThat(candidature.getStatut()).isEqualTo(Candidature.Statut.EN_ATTENTE);
        assertThat(candidature.getDatePostulation()).isNotNull();
    }

    @Test
    void shouldTestSettersAndGetters() {
        Candidature candidature = new Candidature();
        LocalDateTime now = LocalDateTime.now();

        candidature.setId(2L);
        candidature.setCandidatId(11L);
        candidature.setOffreId(21L);
        candidature.setScoreMatching(90);
        candidature.setStatut(Candidature.Statut.ACCEPTE);
        candidature.setDatePostulation(now);

        assertThat(candidature.getId()).isEqualTo(2L);
        assertThat(candidature.getCandidatId()).isEqualTo(11L);
        assertThat(candidature.getOffreId()).isEqualTo(21L);
        assertThat(candidature.getScoreMatching()).isEqualTo(90);
        assertThat(candidature.getStatut()).isEqualTo(Candidature.Statut.ACCEPTE);
        assertThat(candidature.getDatePostulation()).isEqualTo(now);
    }

    @Test
    void shouldTestEnumValues() {
        assertThat(Candidature.Statut.values()).containsExactly(
                Candidature.Statut.EN_ATTENTE,
                Candidature.Statut.ACCEPTE,
                Candidature.Statut.REFUSE,
                Candidature.Statut.ENTRETIEN
        );
        assertThat(Candidature.Statut.valueOf("EN_ATTENTE")).isEqualTo(Candidature.Statut.EN_ATTENTE);
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Candidature c1 = Candidature.builder().id(1L).candidatId(10L).offreId(20L).scoreMatching(85).statut(Candidature.Statut.EN_ATTENTE).datePostulation(now).build();
        Candidature c2 = Candidature.builder().id(1L).candidatId(10L).offreId(20L).scoreMatching(85).statut(Candidature.Statut.EN_ATTENTE).datePostulation(now).build();
        Candidature c3 = Candidature.builder().id(2L).candidatId(11L).offreId(21L).scoreMatching(90).statut(Candidature.Statut.ACCEPTE).datePostulation(now).build();

        assertThat(c1).isEqualTo(c2);
        assertThat(c1).isNotEqualTo(c3);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        assertThat(c1.hashCode()).isNotEqualTo(c3.hashCode());
    }

    @Test
    void shouldTestToString() {
        Candidature candidature = Candidature.builder().id(1L).candidatId(10L).build();
        assertThat(candidature.toString()).contains("id=1", "candidatId=10");
    }

    @Test
    void shouldTestAllArgsConstructorAndNoArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Candidature candidature = new Candidature(1L, 10L, 20L, 85, Candidature.Statut.EN_ATTENTE, now);
        assertThat(candidature.getId()).isEqualTo(1L);
        assertThat(candidature.getCandidatId()).isEqualTo(10L);
        assertThat(candidature.getOffreId()).isEqualTo(20L);
        assertThat(candidature.getScoreMatching()).isEqualTo(85);
        assertThat(candidature.getStatut()).isEqualTo(Candidature.Statut.EN_ATTENTE);
        assertThat(candidature.getDatePostulation()).isEqualTo(now);
    }
}