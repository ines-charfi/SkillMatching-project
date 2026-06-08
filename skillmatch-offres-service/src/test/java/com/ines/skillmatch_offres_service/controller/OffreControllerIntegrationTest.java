package com.ines.skillmatch_offres_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.repository.OffreRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OffreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        offreRepository.deleteAll(); // Nettoyage de la base H2 avant chaque test
    }

    @Test
    void testGetOffreById_ShouldReturnOffre() throws Exception {
        // Insertion d'une offre en base H2
        Offre offre = new Offre();
        offre.setTitre("Développeur Java");
        offre.setDescription("CDI à Tunis");
        offre = offreRepository.save(offre);

        mockMvc.perform(get("/api/offres/" + offre.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Développeur Java"));
    }

    @Test
    void testUpdateOffre_ShouldReturnUpdatedOffre() throws Exception {
        // Insertion initiale
        Offre offre = new Offre();
        offre.setTitre("Stage PFE");
        offre.setDescription("Sujet Microservices");
        offre = offreRepository.save(offre);

        // Données de mise à jour
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("titre", "Stage PFE - SkillMatch");
        updatePayload.put("description", "Sujet Microservices et Docker");

        mockMvc.perform(put("/api/offres/" + offre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Stage PFE - SkillMatch"));
    }
}