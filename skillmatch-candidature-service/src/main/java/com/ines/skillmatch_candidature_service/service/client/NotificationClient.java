package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "skillmatch-auth-service") // Il cible l'auth-service qui héberge MongoDB
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void envoyerNotification(@RequestBody Map<String, Object> notificationData);
}
