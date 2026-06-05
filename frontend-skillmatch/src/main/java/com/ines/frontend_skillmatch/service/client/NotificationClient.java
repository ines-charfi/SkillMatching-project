package com.ines.frontend_skillmatch.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-auth-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void envoyerNotification(@RequestBody Map<String, Object> notificationData);

    // 🎯 FIX : Ajout du @RequestParam("role") pour filtrer les notifications du candidat ou recruteur
    @GetMapping("/api/notifications/user/{userId}")
    List<Map<String, Object>> getNotifications(
            @PathVariable("userId") Long userId,
            @RequestParam("role") String role
    );

    // 🎯 FIX : Ajout du @RequestParam("role") pour compter correctement les non lues
    @GetMapping("/api/notifications/user/{userId}/count")
    Long countNonLues(
            @PathVariable("userId") Long userId,
            @RequestParam("role") String role
    );

    @PutMapping("/api/notifications/{id}/lire")
    void marquerCommeLue(@PathVariable("id") String id);
}