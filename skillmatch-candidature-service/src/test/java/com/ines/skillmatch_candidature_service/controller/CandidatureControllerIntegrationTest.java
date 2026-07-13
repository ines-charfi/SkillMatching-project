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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        mockMvc.perform(post("/api/candidatures/entreprise/entretiens/planifier")
                        .param("candidatureId", "999")
                        .param("date", "2026-06-12T11:15")
                        .param("lieu", "Nulle part")
                        .param("notes", "Error Test Case")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetByCandidat_ShouldReturnList() throws Exception {
        // 🔧 1. Insérer deux candidatures pour le candidat 1
        Candidature c1 = new Candidature();
        c1.setCandidatId(1L);
        c1.setOffreId(10L);
        c1.setStatut(Candidature.Statut.EN_ATTENTE);
        c1.setScoreMatching(75);
        candidatureRepository.save(c1);

        Candidature c2 = new Candidature();
        c2.setCandidatId(1L);
        c2.setOffreId(20L);
        c2.setStatut(Candidature.Statut.ACCEPTE);
        c2.setScoreMatching(90);
        candidatureRepository.save(c2);

        // 🔧 2. Appeler l'endpoint et vérifier que la réponse contient 2 éléments
        mockMvc.perform(get("/api/candidatures/candidat/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}