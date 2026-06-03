package com.ines.frontend_skillmatch.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-auth-service", url = "http://auth-service:8081")
public interface AuthClient {

    // --- Routes publiques d'authentification ---
    @PostMapping("/api/auth/login")
    Map<String, Object> login(@RequestBody Map<String, String> credentials);

    @PostMapping("/api/auth/register")
    Map<String, Object> register(@RequestBody Map<String, Object> registrationData);

    // Attention, dans ton AuthController backend c'est "/api/auth/stats/public" !
    @GetMapping("/api/auth/stats/public")
    Map<String, Object> getPublicStats();


    // --- Routes d'administration (Pointent vers l'AdminController du Backend) ---
    @GetMapping("/api/admin/stats")
    Map<String, Object> getAdminStats();

    @GetMapping("/api/admin/users")
    List<Map<String, Object>> getAllUsers();


    @GetMapping("/api/admin/fichiers-a-verifier")
    List<Map<String, Object>> getFichiersAVerifier();

    @PutMapping("/api/admin/users/{id}/toggle")
    Void toggleUserStatus(@PathVariable("id") Long id);
}