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

    @Override
    public void envoyerNotification(Map<String, Object> notificationData) {
        // Si le service de notification est en panne, on log l'erreur pour ne pas bloquer l'action principale (ex: une candidature)
        logger.error("[Fallback] Impossible d'envoyer la notification. Le service est hors-ligne. Données : {}", notificationData);
    }

    @Override
    public List<Map<String, Object>> getNotifications(Long userId, String role) {
        // Renvoie une liste vide pour éviter un crash de l'interface utilisateur
        return new ArrayList<>();
    }

    @Override
    public Long countNonLues(Long userId, String role) {
        // Si le service est en panne, la cloche affichera 0 notification au lieu de faire planter le header du site
        return 0L;
    }

    @Override
    public void marquerCommeLue(String id) {
        logger.warn("[Fallback] Impossible de marquer la notification {} comme lue. Le service est indisponible.", id);
    }
}