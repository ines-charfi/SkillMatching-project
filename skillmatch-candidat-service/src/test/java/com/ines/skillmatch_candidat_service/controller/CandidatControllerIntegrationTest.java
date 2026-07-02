package com.ines.skillmatch_candidat_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ines.skillmatch_candidat_service.model.Candidat;
import com.ines.skillmatch_candidat_service.repository.CandidatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Utilise la base de données H2 configurée précédemment
class CandidatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        candidatRepository.deleteAll(); // On vide la table H2 avant chaque exécution de test
    }

    @Test
    void testGetCandidatProfile_ShouldReturnCandidat() throws Exception {
        // On insère manuellement un candidat en base H2 pour le test
        Candidat candidat = new Candidat();
        candidat.setNom("Charfi Saja");
        candidat = candidatRepository.save(candidat);

        // Appel de l'API REST
        mockMvc.perform(get("/api/candidats/" + candidat.getId()) // Adapte l'URL de ton controlleur
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Charfi Saja"));
    }

    @Test
    void testCreateCandidatProfile_ShouldReturnCreated() throws Exception {
        // Simulation du corps JSON envoyé par le Frontend (ou via un DTO)
        Map<String, Object> candidatDto = new HashMap<>();
        candidatDto.put("nom", "Ines Dev");
        candidatDto.put("telephone", "0700000000");

        mockMvc.perform(post("/api/candidats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidatDto)))
                .andExpect(status().isCreated()) // Ou .isOk() suivant ton implémentation
                .andExpect(jsonPath("$.nom").value("Ines Dev"));
    }
}