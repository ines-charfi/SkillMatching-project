package com.ines.frontend_skillmatch.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// CORRECTION : Changement de 'name' pour matcher Consul et suppression de l'URL en dur
@FeignClient(name = "skillmatch-auth-service")
public interface AuthClient {

    @PostMapping("/api/auth/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);

    @PostMapping("/api/auth/register")
    Map<String, Object> register(@RequestBody Map<String, Object> registrationData);

    @GetMapping("/api/auth/stats")
    Map<String, Object> getPublicStats();

    @GetMapping("/api/auth/admin/stats")
    Map<String, Object> getAdminStats();

    @GetMapping("/api/auth/admin/users")
    List<Map<String, Object>> getAllUsers();

    @GetMapping("/api/auth/admin/fichiers-verification")
    List<Map<String, Object>> getFichiersAVerifier();

    @PostMapping("/api/auth/admin/users/{id}/toggle-status")
    Map<String, Object> toggleUserStatus(@PathVariable("id") Long id);
}