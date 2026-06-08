package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import com.ines.skillmatch_candidature_service.service.client.NotificationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidatureServiceUnitTestSimplifie {

    @Mock private CandidatureRepository candidatureRepository;
    @Mock private MatchingService matchingService;
    @Mock private CandidatClient candidatClient;
    @Mock private OffreClient offreClient;
    @Mock private NotificationClient notificationClient;
    @Mock private com.ines.skillmatch_candidature_service.repository.EntretienRepository entretienRepository;

    @InjectMocks private CandidatureService candidatureService;

    @Test
    void testPostuler_OK() {
        // Données simulées
        Map<String, Object> offre = new HashMap<>();
        offre.put("competencesRequises", "Java");
        offre.put("niveauRequis", "BAC+3");

        Map<String, Object> candidat = new HashMap<>();
        candidat.put("competences", "Java");
        candidat.put("niveauScolaire", "BAC+5");

        // Comportement des mocks
        when(candidatureRepository.existsByCandidatIdAndOffreId(1L, 10L)).thenReturn(false);
        when(candidatClient.getProfil(1L)).thenReturn(candidat);
        when(offreClient.getOffre(10L)).thenReturn(offre);
        when(matchingService.calculateScore(any(), any(), any(), any())).thenReturn(75);
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArgument(0));

        // Appel de la méthode réelle
        Candidature resultat = candidatureService.postuler(1L, 10L);

        // Vérifications
        assertNotNull(resultat);
        assertEquals(75, resultat.getScoreMatching());
        assertEquals(Candidature.Statut.EN_ATTENTE, resultat.getStatut());

        verify(candidatureRepository).save(any(Candidature.class));
        verify(notificationClient).envoyerNotification(anyMap());
    }

    @Test
    void testPostuler_dejaPostule_doitLeverException() {
        when(candidatureRepository.existsByCandidatIdAndOffreId(1L, 10L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> candidatureService.postuler(1L, 10L));
        verify(candidatureRepository, never()).save(any());
    }

    @Test
    void testUpdateStatut_changeStatut() {
        Candidature candidatureExistante = Candidature.builder()
                .id(5L)
                .candidatId(1L)
                .offreId(10L)
                .statut(Candidature.Statut.EN_ATTENTE)
                .build();

        when(candidatureRepository.findById(5L)).thenReturn(Optional.of(candidatureExistante));
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArgument(0));
        when(offreClient.getOffre(10L)).thenReturn(new HashMap<>()); // juste pour éviter NullPointer

        Candidature misAJour = candidatureService.updateStatut(5L, "ACCEPTE");

        assertEquals(Candidature.Statut.ACCEPTE, misAJour.getStatut());
        verify(candidatureRepository).save(candidatureExistante);
        verify(notificationClient).envoyerNotification(anyMap());
    }
}