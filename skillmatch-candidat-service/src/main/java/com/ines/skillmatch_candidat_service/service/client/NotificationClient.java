package com.ines.skillmatch_candidat_service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "skillmatch-auth-service", path = "/api/notifications")
public interface NotificationClient {
    /**
     * Sends a notification by forwarding a payload to the notification endpoint
     * of the auth service.
     *
     * The HTTP method is POST, and the payload is sent as a JSON object in the
     * request body.
     *
     * Typical payload fields might include:
     * - "type": the notification type (e.g., "EMAIL", "SMS")
     * - "recipient": the target user's email or phone number
     * - "subject": the subject of the notification (for emails)
     * - "body": the content/message of the notification
     * - "userId": the user ID associated with the notification
     *
     * The auth service is expected to process this payload and deliver the
     * notification accordingly.
     *
     * @param payload a Map containing the notification data (keys and values
     *                depend on the contract with the auth service)
     */
    @PostMapping
    void envoyerNotification(@RequestBody Map<String, Object> payload);
}