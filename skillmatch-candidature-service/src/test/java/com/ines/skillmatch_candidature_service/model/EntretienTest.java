package com.ines.skillmatch_candidature_service.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class EntretienTest {

    @Test
    void shouldCreateEntretienUsingBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Entretien entretien = Entretien.builder()
                .id(1L)
                .candidatureId(100L)
                .dateEntretien(now)
                .lieu("Paris")
                .notes("Préparer les questions techniques")
                .statut(Entretien.Statut.PROGRAMME)
                .build();

        assertThat(entretien.getId()).isEqualTo(1L);
        assertThat(entretien.getCandidatureId()).isEqualTo(100L);
        assertThat(entretien.getDateEntretien()).isEqualTo(now);
        assertThat(entretien.getLieu()).isEqualTo("Paris");
        assertThat(entretien.getNotes()).isEqualTo("Préparer les questions techniques");
        assertThat(entretien.getStatut()).isEqualTo(Entretien.Statut.PROGRAMME);
    }

    @Test
    void shouldUseDefaultValues() {
        Entretien entretien = new Entretien();
        assertThat(entretien.getStatut()).isEqualTo(Entretien.Statut.PROGRAMME);
    }

    @Test
    void shouldTestSettersAndGetters() {
        Entretien entretien = new Entretien();
        LocalDateTime now = LocalDateTime.now();

        entretien.setId(2L);
        entretien.setCandidatureId(101L);
        entretien.setDateEntretien(now);
        entretien.setLieu("Lyon");
        entretien.setNotes("Apportez votre CV");
        entretien.setStatut(Entretien.Statut.TERMINE);

        assertThat(entretien.getId()).isEqualTo(2L);
        assertThat(entretien.getCandidatureId()).isEqualTo(101L);
        assertThat(entretien.getDateEntretien()).isEqualTo(now);
        assertThat(entretien.getLieu()).isEqualTo("Lyon");
        assertThat(entretien.getNotes()).isEqualTo("Apportez votre CV");
        assertThat(entretien.getStatut()).isEqualTo(Entretien.Statut.TERMINE);
    }

    @Test
    void shouldTestEnumValues() {
        assertThat(Entretien.Statut.values()).containsExactly(
                Entretien.Statut.PROGRAMME,
                Entretien.Statut.TERMINE,
                Entretien.Statut.ANNULE
        );
        assertThat(Entretien.Statut.valueOf("PROGRAMME")).isEqualTo(Entretien.Statut.PROGRAMME);
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Entretien e1 = Entretien.builder().id(1L).candidatureId(100L).dateEntretien(now).lieu("Paris").statut(Entretien.Statut.PROGRAMME).build();
        Entretien e2 = Entretien.builder().id(1L).candidatureId(100L).dateEntretien(now).lieu("Paris").statut(Entretien.Statut.PROGRAMME).build();
        Entretien e3 = Entretien.builder().id(2L).candidatureId(101L).dateEntretien(now).lieu("Lyon").statut(Entretien.Statut.TERMINE).build();

        assertThat(e1).isEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        assertThat(e1.hashCode()).isNotEqualTo(e3.hashCode());
    }

    @Test
    void shouldTestToString() {
        Entretien entretien = Entretien.builder().id(1L).candidatureId(100L).build();
        assertThat(entretien.toString()).contains("id=1", "candidatureId=100");
    }

    @Test
    void shouldTestAllArgsConstructorAndNoArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Entretien entretien = new Entretien(1L, 100L, now, "Paris", "Notes", Entretien.Statut.PROGRAMME);
        assertThat(entretien.getId()).isEqualTo(1L);
        assertThat(entretien.getCandidatureId()).isEqualTo(100L);
        assertThat(entretien.getDateEntretien()).isEqualTo(now);
        assertThat(entretien.getLieu()).isEqualTo("Paris");
        assertThat(entretien.getNotes()).isEqualTo("Notes");
        assertThat(entretien.getStatut()).isEqualTo(Entretien.Statut.PROGRAMME);
    }
}