package com.ines.skillmatch_offres_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

// Feign client for the authentication service's notification endpoint.
// Uses Consul service discovery with the logical name 'skillmatch-auth-service'.
@FeignClient(name = "skillmatch-auth-service", path = "/api/notifications")
public interface NotificationClient {

    // Sends a notification to the auth service, which persists it in MongoDB for the target user.
    @PostMapping
    void envoyerNotification(@RequestBody Map<String, Object> payload);
}