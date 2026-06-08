package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    @Test
    void testGetScore_ShouldReturnScore() throws Exception {
        // Given
        Long userId = 1L;
        Long offreId = 10L;
        int expectedScore = 85;

        when(matchingService.generateFullScore(userId, offreId)).thenReturn(expectedScore);

        // When & Then
        mockMvc.perform(get("/api/matching/score")
                        .param("userId", userId.toString())
                        .param("offreId", offreId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedScore)));
    }

    @Test
    void testGetScore_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        // Pas de paramètre userId
        mockMvc.perform(get("/api/matching/score")
                        .param("offreId", "10"))
                .andExpect(status().isBadRequest());

        // Pas de paramètre offreId
        mockMvc.perform(get("/api/matching/score")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());
    }
}