package com.ines.skillmatch_auth_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
public class Notification {
    @Id
    private String id;
    private Long userIdTarget;
    private String recipientRole; //  FIX : 'candidate' or 'recruiter'
    private String titreNotif;
    private String message;
    private boolean lu = false;

    @Indexed(expireAfterSeconds = 604800) // Supprime automatiquement après 7 jours
    private LocalDateTime dateCreation = LocalDateTime.now();
}