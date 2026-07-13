package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client interface for inter-service communication.
 * This client allows the Candidature service to communicate with the Auth service.
 */
@FeignClient(name = "skillmatch-auth-service") // Targets the auth-service which hosts MongoDB
public interface NotificationClient {

    /**
     * Sends a notification by making a POST request to the authentication service.
     *
     * @param notificationData A map containing the notification details (e.g., recipient, message, type).
     */
    @PostMapping("/api/notifications")
    void envoyerNotification(@RequestBody Map<String, Object> notificationData);
}