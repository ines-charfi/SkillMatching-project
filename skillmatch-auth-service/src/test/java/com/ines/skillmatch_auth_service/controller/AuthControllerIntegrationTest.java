package com.ines.skillmatch_auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Utilise la base de données H2 définie dans tes ressources de test
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll(); // On nettoie la base H2 avant chaque test

        // On pré-insère un utilisateur de test directement en base
        User testUser = new User();
        testUser.setEmail("test@skillmatch.com");
        testUser.setPassword(passwordEncoder.encode("secret123"));
        testUser.setRole(User.Role.valueOf("ENTREPRISE"));
        userRepository.save(testUser);
    }

    @Test
    void testLogin_Success_ShouldReturnToken() throws Exception {
        // Préparation du corps de la requête de Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@skillmatch.com");
        loginRequest.put("password", "secret123");

        mockMvc.perform(post("/api/auth/login") // Remplace par ton URL exacte de login
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                // Si ton backend renvoie un JSON contenant le token ou l'email, on le valide ici :
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegister_Success_ShouldCreateUser() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "nouveau@skillmatch.com");
        registerRequest.put("password", "password123");
        registerRequest.put("role", "CANDIDAT");

        mockMvc.perform(post("/api/auth/register") // Remplace par ton URL exacte de inscription
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated()); // Ou .isOk() selon ton code
    }
}
