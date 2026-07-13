package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for the Matching Engine.
 * Exposes endpoints to calculate and retrieve compatibility scores between candidates and job offers.
 */
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    // Injection of the service handling the core matching algorithm
    private final MatchingService matchingService;

    /**
     * Endpoint to allow the Frontend to request a specific matching score via HTTP.
     * Calculates the semantic proximity between a candidate's profile and a job offer's requirements.
     *
     * @param userId The unique identifier of the user (candidate)
     * @param offreId The unique identifier of the job offer
     * @return An integer representing the calculated matching percentage (0-100)
     */
    @GetMapping("/score")
    public int getScore(@RequestParam Long userId, @RequestParam Long offreId) {
        return matchingService.generateFullScore(userId, offreId);
    }
}