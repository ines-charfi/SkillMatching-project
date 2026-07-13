package com.ines.skillmatch_candidature_service.service;

import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class MatchingServiceTest {
    @ExtendWith(MockitoExtension.class)

        @InjectMocks
        private MatchingService matchingService;

        @Test
        void testCalculateScore_PerfectMatch() {
            // Préparer listes de compétences
            String offres = "Java, Spring";
            String candidat = "Java, Spring";
            String niveauCandidat = "BAC+5";
            String niveauOffre = "BAC+5";

            int score = matchingService.calculateScore(offres, candidat, niveauCandidat, niveauOffre);
            assertThat(score).isEqualTo(100);
        }

        @Test
        void testCalculateScore_PartialMatch() {
            // etc.
        }

        @Test
        void testCalculateScore_NoMatch() {
            // etc.
        }
    @Mock
    private CandidatClient candidatClient;
    @Mock private OffreClient offreClient;


    @Test
    void testGenerateFullScore_ShouldReturnScore() {
        Long userId = 1L;
        Long offreId = 10L;

        Map<String, Object> profil = new HashMap<>();
        profil.put("competences", "Java, Spring");
        profil.put("niveauScolaire", "BAC+5");

        Map<String, Object> offre = new HashMap<>();
        offre.put("competencesRequises", "Java, Spring");
        offre.put("niveauRequis", "BAC+5");

        when(candidatClient.getProfil(userId)).thenReturn(profil);
        when(offreClient.getOffre(offreId)).thenReturn(offre);

        int score = matchingService.generateFullScore(userId, offreId);

        assertThat(score).isEqualTo(100);
    }

    @Test
    void testGenerateFullScore_WhenClientThrowsException_ShouldReturn0() {
        when(candidatClient.getProfil(anyLong())).thenThrow(new RuntimeException("Service down"));
        int score = matchingService.generateFullScore(1L, 10L);
        assertThat(score).isEqualTo(0);
    }
    }

