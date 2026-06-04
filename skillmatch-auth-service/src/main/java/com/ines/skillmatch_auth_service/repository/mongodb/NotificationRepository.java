package com.ines.skillmatch_auth_service.repository.repository.mongodb;


import com.ines.skillmatch_auth_service.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdTargetOrderByDateCreationDesc(Long userIdTarget);
    long countByUserIdTargetAndLuFalse(Long userIdTarget);
}
