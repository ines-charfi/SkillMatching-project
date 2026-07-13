package com.ines.frontend_skillmatch.service.client;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NotificationClientFallback implements NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClientFallback.class);

    // Fallback for sending notifications: logs the error without blocking the main action.
    @Override
    public void envoyerNotification(Map<String, Object> notificationData) {
        logger.error("[Fallback] Impossible d'envoyer la notification. Le service est hors-ligne. Données : {}", notificationData);
    }

    // Fallback for fetching notifications: returns an empty list to avoid UI crashes.
    @Override
    public List<Map<String, Object>> getNotifications(Long userId, String role) {
        return new ArrayList<>();
    }

    // Fallback for counting unread notifications: returns 0 to avoid breaking the UI header.
    @Override
    public Long countNonLues(Long userId, String role) {
        return 0L;
    }

    // Fallback for marking notification as read: logs a warning.
    @Override
    public void marquerCommeLue(String id) {
        logger.warn("[Fallback] Impossible de marquer la notification {} comme lue. Le service est indisponible.", id);
    }
}