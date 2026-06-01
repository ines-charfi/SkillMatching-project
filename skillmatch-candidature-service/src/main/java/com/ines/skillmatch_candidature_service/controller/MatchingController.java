package com.ines.skillmatch_candidature_service.controller;

import com.ines.skillmatch_candidature_service.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    // Cet endpoint va permettre au Frontend de demander le score par HTTP
    @GetMapping("/score")
    public int getScore(@RequestParam Long userId, @RequestParam Long offreId) {
        return matchingService.generateFullScore(userId, offreId);
    }
}
