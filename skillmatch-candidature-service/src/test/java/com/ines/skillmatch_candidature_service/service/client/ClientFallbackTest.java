package com.ines.skillmatch_candidature_service.service.client;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ClientFallbackTest {

    @Test
    void testCandidatClientFallback_ShouldReturnEmptyProfil() {
        CandidatClientFallback fallback = new CandidatClientFallback();
        var profil = fallback.getProfil(1L);
        assertThat(profil).isNull();
    }

    @Test
    void testOffreClientFallback_ShouldReturnEmptyOffre() {
        OffreClientFallback fallback = new OffreClientFallback();
        var offre = fallback.getOffre(10L);
        assertThat(offre).isNull();
    }

    @Test
    void testOffreClientFallback_GetOffresByEntreprise_ShouldReturnEmptyList() {
        OffreClientFallback fallback = new OffreClientFallback();
        var list = fallback.getOffresByEntreprise(1L);
        assertThat(list).isEmpty();
    }

    @Test
    void testNotificationClientFallback_ShouldDoNothing() {
        NotificationClientFallback fallback = new NotificationClientFallback();
        // On vérifie qu'aucune exception n'est levée
        fallback.envoyerNotification(null);
    }
}