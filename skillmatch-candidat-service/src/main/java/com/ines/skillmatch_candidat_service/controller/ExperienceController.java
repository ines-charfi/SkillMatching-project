package com.ines.skillmatch_candidat_service.controller;

import com.ines.skillmatch_candidat_service.dto.ExperienceDTO;
import com.ines.skillmatch_candidat_service.model.Experience;
import com.ines.skillmatch_candidat_service.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidats/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Experience> addExperience(@PathVariable Long userId, @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.addExperience(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.updateExperience(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        experienceService.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Experience>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(experienceService.getExperiencesByUserId(userId));
    }
}