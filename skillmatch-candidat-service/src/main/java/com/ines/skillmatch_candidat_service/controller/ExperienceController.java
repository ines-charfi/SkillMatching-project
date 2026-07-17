package com.ines.skillmatch_candidat_service.controller;

import com.ines.skillmatch_candidat_service.dto.ExperienceDTO;
import com.ines.skillmatch_candidat_service.model.Experience;
import com.ines.skillmatch_candidat_service.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * REST controller for managing professional experiences (Experience) associated with candidates.
 * All endpoints are prefixed with "/api/candidats/experiences".
 */
@RestController
@RequestMapping("/api/candidats/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;
    /**
     * Adds a new professional experience entry for a specific candidate.
     *
     * @param userId the ID of the user (candidate) to whom the experience belongs
     * @param dto    the experience data transfer object containing details like title, company, dates, etc.
     * @return the created Experience entity with its generated ID
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<Experience> addExperience(@PathVariable Long userId, @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.addExperience(userId, dto));
    }
    /**
     * Updates an existing experience entry identified by its ID.
     *
     * @param id  the ID of the experience to update
     * @param dto the new experience data
     * @return the updated Experience entity
     */
    @PutMapping("/{id}")
    public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.updateExperience(id, dto));
    }
    /**
     * Deletes an experience entry by its ID.
     *
     * @param id the ID of the experience to delete
     * @return HTTP 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        experienceService.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Retrieves all professional experiences associated with a given user (candidate).
     *
     * @param userId the user ID
     * @return a list of Experience entities belonging to that user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Experience>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(experienceService.getExperiencesByUserId(userId));
    }
}