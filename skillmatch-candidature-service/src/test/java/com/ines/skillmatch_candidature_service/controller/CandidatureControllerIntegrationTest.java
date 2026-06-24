package com.ines.skillmatch_candidature_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import com.ines.skillmatch_candidature_service.service.client.CandidatClient;
import com.ines.skillmatch_candidature_service.service.client.OffreClient;
import com.ines.skillmatch_candidature_service.service.client.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// 1. 🟢 IMPORT CRITIQUE : Permet de contourner le blocage du jeton anti-falsification (CSRF) de Spring Security
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(authorities = "ROLE_ENTREPRISE")
class CandidatureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CandidatClient candidatClient;

    @MockBean
    private OffreClient offreClient;

    @MockBean
    private NotificationClient notificationClient;

    @BeforeEach
    void cleanUp() {
        candidatureRepository.deleteAll();
    }

    @Test
    void testGetAllCandidatures_ShouldReturnList() throws Exception {
        Candidature candidature = new Candidature();
        candidature.setOffreId(10L);
        candidature.setCandidatId(5L);
        candidature.setStatut(Candidature.Statut.EN_ATTENTE);
        candidature.setScoreMatching(80);
        candidatureRepository.save(candidature);

        mockMvc.perform(get("/api/candidatures/candidat/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].statut").value("EN_ATTENTE"));
    }

    @Test
    void testSoumettreCandidature_ShouldReturnCreated() throws Exception {
        Map<String, Object> candidatMock = new HashMap<>();
        candidatMock.put("competences", "Java");
        candidatMock.put("niveauScolaire", "BAC+5");

        Map<String, Object> offreMock = new HashMap<>();
        offreMock.put("competencesRequises", "Java");
        offreMock.put("niveauRequis", "BAC+3");
        offreMock.put("entrepriseId", 1L);

        Mockito.when(candidatClient.getProfil(3L)).thenReturn(candidatMock);
        Mockito.when(offreClient.getOffre(12L)).thenReturn(offreMock);

        // 2. 🟢 CORRECTION : Ajout du jeton CSRF pour passer la barrière de sécurité en POST
        mockMvc.perform(post("/api/candidatures")
                        .param("candidatId", "3")
                        .param("offreId", "12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
    }

    @Test
    void testUpdateStatut_CandidatureIntrouvable_ShouldReturnNotFound() throws Exception {
        Map<String, String> updateBody = new HashMap<>();
        updateBody.put("statut", "ACCEPTE");

        // 3. 🟢 CORRECTION : Ajout du jeton CSRF pour passer la barrière de sécurité en PATCH
        mockMvc.perform(patch("/api/candidatures/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPlanifierEntretien_Success_ShouldReturnOk() throws Exception {
        Candidature candidature = new Candidature();
        candidature.setOffreId(10L);
        candidature.setCandidatId(5L);
        candidature.setStatut(Candidature.Statut.EN_ATTENTE);
        candidatureRepository.save(candidature);

        // 4. 🟢 CORRECTION : Ajout du jeton CSRF
        mockMvc.perform(post("/api/candidatures/entreprise/entretiens/planifier")
                        .param("candidatureId", candidature.getId().toString())
                        .param("date", "2026-06-12T11:15")
                        .param("lieu", "Paris / Distanciel")
                        .param("notes", "Entretien technique Java")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void testPlanifierEntretien_CandidatureIntrouvable_ShouldThrowException() throws Exception {
        // Plus besoin de assertThrows ! On vérifie le comportement de l'API directement
        mockMvc.perform(post("/api/candidatures/entreprise/entretiens/planifier")
                        .param("candidatureId", "999")
                        .param("date", "2026-06-12T11:15")
                        .param("lieu", "Nulle part")
                        .param("notes", "Test Erreur")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // Ou .isInternalServerError() selon ton GlobalExceptionHandler
    }
}