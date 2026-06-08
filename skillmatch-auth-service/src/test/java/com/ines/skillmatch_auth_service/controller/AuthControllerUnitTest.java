package com.ines.skillmatch_auth_service.controller;

import com.ines.skillmatch_auth_service.dto.AuthResponse;
import com.ines.skillmatch_auth_service.dto.LoginRequest;
import com.ines.skillmatch_auth_service.dto.RegisterRequest;
import com.ines.skillmatch_auth_service.model.User;
import com.ines.skillmatch_auth_service.repository.jpa.UserRepository;
import com.ines.skillmatch_auth_service.service.AuthService;
import com.ines.skillmatch_auth_service.service.client.OffreClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.ines.skillmatch_auth_service.controller.AuthController.class)
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OffreClient offreClient;

    @Test
    void testRegister() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .message("Inscription réussie")
                .email("test@example.com")
                .role("CANDIDAT")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password\",\"role\":\"CANDIDAT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.message").value("Inscription réussie"));
    }

    @Test
    void testLogin() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .message("Connexion réussie")
                .email("test@example.com")
                .role("CANDIDAT")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void testOauth2Success() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/success")
                        .param("token", "oauth-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("oauth-token"))
                .andExpect(jsonPath("$.message").value("Connexion OAuth2 réussie"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déconnexion réussie"));
    }

    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vous êtes connecté"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testGetAllUsers_AsAdmin() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("admin@test.com")
                .role(User.Role.ADMIN)
                .enabled(true)
                .provider("LOCAL")
                .dateCreation(LocalDateTime.now())
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testGetUserById_AsAdmin() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .role(User.Role.CANDIDAT)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testToggleUserStatus() throws Exception {
        User user = User.builder().id(1L).enabled(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/auth/users/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testChangeUserRole() throws Exception {
        User user = User.builder().id(1L).role(User.Role.CANDIDAT).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/api/auth/users/1/role")
                        .param("role", "ENTREPRISE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ENTREPRISE"));
    }

    @Test
    void testGetPublicStats() throws Exception {
        when(userRepository.count()).thenReturn(10L);
        when(offreClient.countAllOffres()).thenReturn(5L);
        when(userRepository.countByRole(User.Role.ENTREPRISE)).thenReturn(3L);

        mockMvc.perform(get("/api/auth/stats/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalOffres").value(5))
                .andExpect(jsonPath("$.totalEntreprises").value(3));
    }
}