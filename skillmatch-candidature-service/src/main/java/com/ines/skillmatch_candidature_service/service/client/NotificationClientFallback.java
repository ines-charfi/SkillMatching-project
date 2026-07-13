package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.stereotype.Component;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback implementation for the NotificationClient.
 * This class is triggered when the target notification service is unreachable or fails,
 * ensuring the main application flow remains uninterrupted.
 */
@Component
public class NotificationClientFallback implements NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClientFallback.class);

    /**
     * Handles the notification failure gracefully.
     * Instead of throwing an exception and blocking the process, it logs the incident.
     */
    @Override
    public void envoyerNotification(Map<String, Object> notificationData) {
        // In case of notification service failure, we do not block the application process!
        // We log the error so administrators are aware, but the user experience remains smooth.
        logger.error("The Notification service is down. Unable to send alert for: {}", notificationData);
    }
}