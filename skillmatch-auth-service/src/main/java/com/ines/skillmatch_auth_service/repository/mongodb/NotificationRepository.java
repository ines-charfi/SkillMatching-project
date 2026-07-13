package com.ines.skillmatch_auth_service.repository.mongodb;

import com.ines.skillmatch_auth_service.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Finds all notifications for a specific user and role, sorted newest first.
    List<Notification> findByUserIdTargetAndRecipientRoleOrderByDateCreationDesc(Long userIdTarget, String recipientRole);

    // Counts unread notifications for a specific user and role.
    long countByUserIdTargetAndRecipientRoleAndLuFalse(Long userIdTarget, String recipientRole);
}