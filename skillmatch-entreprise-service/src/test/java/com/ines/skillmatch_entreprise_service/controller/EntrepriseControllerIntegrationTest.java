package com.ines.skillmatch_entreprise_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.repository.EntrepriseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Integration tests for EntrepriseController – tests all REST endpoints with a real application context.
@SpringBootTest
// Configures MockMvc for testing web layer without starting a full server.
@AutoConfigureMockMvc
// Uses the 'test' profile (e.g., in-memory H2 database, disabled file uploads).
@ActiveProfiles("test")
class EntrepriseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntrepriseRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private Entreprise entrepriseTest;

    // Sets up a test company before each test method.
    @BeforeEach
    void setUp() {
        repository.deleteAll();
        entrepriseTest = Entreprise.builder()
                .userId(100L)
                .nomEntreprise("TechCorp")
                .secteur("Informatique")
                .build();
        repository.save(entrepriseTest);
    }

    // Tests the /init endpoint – ensures a new company profile can be created.
    @Test
    void testInitEntreprise() throws Exception {
        mockMvc.perform(post("/api/entreprises/init")
                        .param("userId", "200")
                        .param("nom", "NewCompany"))
                .andExpect(status().isOk());

        Optional<Entreprise> saved = repository.findByUserId(200L);
        assert(saved.isPresent());
        assert("NewCompany".equals(saved.get().getNomEntreprise()));
    }

    // Tests the /user/{userId} endpoint – verifies a company can be retrieved by user ID.
    @Test
    void testGetByUserId() throws Exception {
        mockMvc.perform(get("/api/entreprises/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomEntreprise").value("TechCorp"));
    }

    // Tests the multipart profile update endpoint – ensures text fields and logo can be updated together.
    @Test
    void testUpdateProfil_WithMultipartData() throws Exception {
        MockMultipartFile logo = new MockMultipartFile("logo", "logo.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/api/entreprises/user/100")
                        .file(logo)
                        .param("nomEntreprise", "UpdatedCorp")
                        .param("secteur", "Cloud")
                        .param("description", "Nouvelle description")
                        .param("siteWeb", "https://new.com")
                        .param("telephone", "0600000000")
                        .param("contactEmail", "contact@new.com")
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomEntreprise").value("UpdatedCorp"))
                .andExpect(jsonPath("$.secteur").value("Cloud"));
    }

    // Tests the logo download endpoint – expects 404 because the file doesn't exist on disk.
    @Test
    void testGetLogo_WhenExists() throws Exception {
        // Create a company with a logo path (but no actual file on disk)
        Entreprise entreprise = Entreprise.builder()
                .userId(300L)
                .nomEntreprise("LogoInc")
                .logoPath("logos/mock.png")
                .build();
        repository.save(entreprise);

        mockMvc.perform(get("/api/entreprises/" + entreprise.getId() + "/logo"))
                .andExpect(status().isNotFound()); // file not actually present on disk
        // In a real scenario with a file present, we'd expect 200 and image content type.
    }
}