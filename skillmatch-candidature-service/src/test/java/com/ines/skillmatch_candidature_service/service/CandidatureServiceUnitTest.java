package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import com.ines.skillmatch_candidature_service.service.client.NotificationClient;
import org.junit.jupiter.api.DisplayName;
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
class CandidatureServiceUnitTest {

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
        offre.put("entrepriseId", 1L);

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
    @DisplayName("TEST-01 : Cas de la non-correspondance absolue (0%)")
    void testMatchingScore_ZeroPercent() {
        // 1. Profils sans compétences communes
        Map<String, Object> offre = new HashMap<>();
        offre.put("competencesRequises", "Java, Angular");
        offre.put("niveauRequis", "BAC+3");
        offre.put("entrepriseId", 1L);

        Map<String, Object> candidat = new HashMap<>();
        candidat.put("competences", "PHP, React");
        candidat.put("niveauScolaire", "BAC+5");

        when(candidatureRepository.existsByCandidatIdAndOffreId(1L, 10L)).thenReturn(false);
        when(candidatClient.getProfil(1L)).thenReturn(candidat);
        when(offreClient.getOffre(10L)).thenReturn(offre);
        // On simule le fait que l'algorithme calcule un score de 0
        when(matchingService.calculateScore(any(), any(), any(), any())).thenReturn(0);
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArgument(0));

        // 2. Exécution
        Candidature resultat = candidatureService.postuler(1L, 10L);

        // 3. Validation
        assertEquals(0, resultat.getScoreMatching(), "Le score de matching devrait être strictement égal à 0%");
    }

    @Test
    @DisplayName("TEST-02 : Cas de l'adéquation parfaite avec traitement sémantique (100%)")
    void testMatchingScore_CentPercent() {
        // 1. Compétences identiques polluées par la casse/espaces
        Map<String, Object> offre = new HashMap<>();
        offre.put("competencesRequises", "Java, Spring");
        offre.put("niveauRequis", "BAC+3");
        offre.put("entrepriseId", 1L);

        Map<String, Object> candidat = new HashMap<>();
        candidat.put("competences", "   java ,  SPRING, MySQL ");
        candidat.put("niveauScolaire", "BAC+3");

        when(candidatureRepository.existsByCandidatIdAndOffreId(1L, 10L)).thenReturn(false);
        when(candidatClient.getProfil(1L)).thenReturn(candidat);
        when(offreClient.getOffre(10L)).thenReturn(offre);
        // On simule le nettoyage et le calcul parfait par ton algorithme
        when(matchingService.calculateScore(any(), any(), any(), any())).thenReturn(100);
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArgument(0));

        // 2. Exécution
        Candidature resultat = candidatureService.postuler(1L, 10L);

        // 3. Validation
        assertEquals(100, resultat.getScoreMatching(), "L'algorithme doit nettoyer la casse et valider une adéquation à 100%");
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

        Map<String, Object> offreMock = new HashMap<>();
        offreMock.put("entrepriseId", 1L);

        when(candidatureRepository.findById(5L)).thenReturn(Optional.of(candidatureExistante));
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArgument(0));
        when(offreClient.getOffre(10L)).thenReturn(offreMock);

        Candidature misAJour = candidatureService.updateStatut(5L, "ACCEPTE");

        assertEquals(Candidature.Statut.ACCEPTE, misAJour.getStatut());
        verify(candidatureRepository).save(candidatureExistante);
        verify(notificationClient).envoyerNotification(anyMap());
    }
}