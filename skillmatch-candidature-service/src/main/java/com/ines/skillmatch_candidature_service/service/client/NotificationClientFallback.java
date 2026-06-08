package com.ines.skillmatch_candidature_service.service.client;

import org.springframework.stereotype.Component;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NotificationClientFallback implements NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClientFallback.class);

    @Override
    public void envoyerNotification(Map<String, Object> notificationData) {
        // En cas de panne du service notification, on ne bloque pas la candidature !
        // On log l'erreur pour que l'administrateur soit au courant, mais l'expérience utilisateur reste fluide.
        logger.error("Le service de Notification est en panne. Impossible d'envoyer l'alerte pour : {}", notificationData);
    }
}
