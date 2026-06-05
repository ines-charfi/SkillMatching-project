package com.ines.skillmatch_auth_service.repository.mongodb;

import com.ines.skillmatch_auth_service.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    // FIX : Filtrage croisé avec l'ID et le rôle du destinataire
    List<Notification> findByUserIdTargetAndRecipientRoleOrderByDateCreationDesc(Long userIdTarget, String recipientRole);
    long countByUserIdTargetAndRecipientRoleAndLuFalse(Long userIdTarget, String recipientRole);
}