package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; // 🟢 Retour au GET original
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser // 🟢 AJOUT : Débloque la sécurité par défaut
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    // 💡 Définition de l'URL de base. Si ton contrôleur utilise une autre URL (ex: "/api/matching"), change-la ici !
    private final String BASE_URL = "/api/matching/score";

    @Test
    void testGetScore_ShouldReturnScore() throws Exception {
        Long userId = 1L;
        Long offreId = 10L;
        int expectedScore = 85;

        when(matchingService.generateFullScore(userId, offreId)).thenReturn(expectedScore);

        mockMvc.perform(get(BASE_URL)
                        .param("userId", userId.toString())
                        .param("offreId", offreId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedScore)));
    }

    @Test
    void testGetScore_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("offreId", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(BASE_URL)
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetScore_ServiceThrowsException_ShouldReturnInternalErrorOrNotFound() throws Exception {
        // 1. On force le service à lever une RuntimeException standard
        when(matchingService.generateFullScore(1L, 1L))
                .thenThrow(new RuntimeException("Erreur de calcul introuvable"));

        // 2. On passe les BONS noms de paramètres (userId et offreId)
        mockMvc.perform(get("/api/matching/score")
                        .param("userId", "1")
                        .param("offreId", "1"))
                // 3. Ton GlobalExceptionHandler va intercepter le mot "introuvable" et renvoyer un 404 !
                .andExpect(status().isNotFound());
    }
}