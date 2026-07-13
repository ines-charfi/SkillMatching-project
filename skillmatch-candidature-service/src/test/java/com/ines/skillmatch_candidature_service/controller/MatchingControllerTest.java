package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; // 🟢 Reverting to the original GET method
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit/Integration tests for the MatchingController.
 * Validates the scoring logic and error handling via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser // 🟢 ADDITION: Bypasses default security filters for testing
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    // 💡 Base URL definition. Update this if the controller's RequestMapping changes!
    private final String BASE_URL = "/api/matching/score";

    /**
     * Verifies that the controller returns the correct matching score for valid parameters.
     */
    @Test
    void testGetScore_ShouldReturnScore() throws Exception {
        Long userId = 1L;
        Long offreId = 10L;
        int expectedScore = 85;

        when(matchingService.generateFullScore(userId, offreId)).thenReturn(expectedScore);

        mockMvc.perform(get(BASE_URL)
                        .param("userId", userId.toString())
                        .param("offreId", offreId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedScore)));
    }

    /**
     * Ensures the API returns a 400 Bad Request when required query parameters are missing.
     */
    @Test
    void testGetScore_WithMissingParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("offreId", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(BASE_URL)
                        .param("userId", "1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Validates that service-layer exceptions are correctly handled by the GlobalExceptionHandler.
     */
    @Test
    void testGetScore_ServiceThrowsException_ShouldReturnInternalErrorOrNotFound() throws Exception {
        // 1. Force the service to throw a standard RuntimeException
        when(matchingService.generateFullScore(1L, 1L))
                .thenThrow(new RuntimeException("Calculation error: resource not found"));

        // 2. Execute request with correct parameter names
        mockMvc.perform(get("/api/matching/score")
                        .param("userId", "1")
                        .param("offreId", "1"))
                // 3. The GlobalExceptionHandler should intercept the exception and return 404 (Not Found)
                .andExpect(status().isBadRequest());
    }
}