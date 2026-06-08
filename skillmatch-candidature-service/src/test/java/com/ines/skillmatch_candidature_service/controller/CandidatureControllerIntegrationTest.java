package com.ines.skillmatch_candidature_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ines.skillmatch_candidature_service.model.Candidature;
import com.ines.skillmatch_candidature_service.repository.CandidatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidatureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        candidatureRepository.deleteAll(); // Nettoie la base H2
    }

    @Test
    void testGetAllCandidatures_ShouldReturnList() throws Exception {
        // Insertion d'une candidature avec l'enum Statut
        Candidature candidature = new Candidature();
        candidature.setOffreId(10L);
        candidature.setCandidatId(5L);
        candidature.setStatut(Candidature.Statut.EN_ATTENTE); // ← enum, pas String
        candidatureRepository.save(candidature);

        // Adapte l'URL selon ton endpoint réel (ex: /api/candidatures/candidat/5)
        // Ici j'imagine que tu as un endpoint pour lister toutes les candidatures
        mockMvc.perform(get("/api/candidatures/candidat/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statut").value("EN_ATTENTE"));
    }

    @Test
    void testSoumettreCandidature_ShouldReturnCreated() throws Exception {
        // Le contrôleur attend des paramètres ?candidatId=...&offreId=...
        // Pas de corps JSON
        mockMvc.perform(post("/api/candidatures")
                        .param("candidatId", "3")
                        .param("offreId", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // postuler retourne 200 OK avec l'objet créé
    }
}