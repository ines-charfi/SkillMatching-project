package com.ines.frontend_skillmatch.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "skillmatch-auth-service", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    // Sends a notification with the provided data (e.g., type, message, recipient).
    @PostMapping("/api/notifications")
    void envoyerNotification(@RequestBody Map<String, Object> notificationData);

    // Retrieves all notifications for a specific user, filtered by role (candidate or recruiter).
    @GetMapping("/api/notifications/user/{userId}")
    List<Map<String, Object>> getNotifications(
            @PathVariable("userId") Long userId,
            @RequestParam("role") String role
    );

    // Counts the number of unread notifications for a user, based on their role.
    @GetMapping("/api/notifications/user/{userId}/count")
    Long countNonLues(
            @PathVariable("userId") Long userId,
            @RequestParam("role") String role
    );

    // Marks a specific notification as read.
    @PutMapping("/api/notifications/{id}/lire")
    void marquerCommeLue(@PathVariable("id") String id);
}