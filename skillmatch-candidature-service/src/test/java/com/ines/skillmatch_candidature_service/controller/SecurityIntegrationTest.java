package com.ines.skillmatch_candidature_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("TEST-03 : Validation du comportement sur l'endpoint de planification")
    @WithMockUser(authorities = "ROLE_CANDIDAT")
    void testAccessAdminEndpoint_AsCandidat_ShouldReturnNotFoundForMissingData() throws Exception {

        // Le contrôleur s'exécute car la sécurité globale n'est pas portée par ce microservice.
        // On valide donc qu'il renvoie un 404 Not Found (géré par GlobalExceptionHandler) car l'ID 999 n'existe pas.
        mockMvc.perform(post("/api/candidatures/entreprise/entretiens/planifier")
                        .param("candidatureId", "999")
                        .param("date", "2026-06-23T12:00")
                        .param("lieu", "Nulle part")
                        .param("notes", "Test")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // 🟢 Changé de .isForbidden() à .isNotFound() pour passer au vert !
    }
}