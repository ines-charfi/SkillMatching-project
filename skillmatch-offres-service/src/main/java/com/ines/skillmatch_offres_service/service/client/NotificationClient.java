package com.ines.skillmatch_offres_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "skillmatch-auth-service", path = "/api/notifications")
public interface NotificationClient {
    @PostMapping
    void envoyerNotification(@RequestBody Map<String, Object> payload);
}