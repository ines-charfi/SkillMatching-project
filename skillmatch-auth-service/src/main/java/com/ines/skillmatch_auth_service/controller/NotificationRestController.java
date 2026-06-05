package com.ines.skillmatch_auth_service.controller;

import com.ines.skillmatch_auth_service.model.Notification;
import com.ines.skillmatch_auth_service.repository.mongodb.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<Notification> creerNotification(@RequestBody java.util.Map<String, Object> payload) {
        Notification notification = new Notification();

        try {
            if (payload.get("userIdTarget") != null) {
                notification.setUserIdTarget(Long.valueOf(payload.get("userIdTarget").toString()));
            }

            // 🎯 FIX : Extraction du rôle du destinataire ('candidate' ou 'recruiter')
            if (payload.get("recipientRole") != null) {
                notification.setRecipientRole((String) payload.get("recipientRole"));
            }

            notification.setTitreNotif((String) payload.get("titreNotif"));
            notification.setMessage((String) payload.get("message"));

            if (payload.get("lu") != null) {
                notification.setLu((Boolean) payload.get("lu"));
            } else {
                notification.setLu(false);
            }

            Notification saved = notificationRepository.save(notification);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la désérialisation de la notification : " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 🎯 FIX : Utilisation du RequestParam pour injecter le filtre du rôle
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long userId, @RequestParam String role) {
        return ResponseEntity.ok(notificationRepository.findByUserIdTargetAndRecipientRoleOrderByDateCreationDesc(userId, role));
    }

    // 🎯 FIX : Utilisation du RequestParam pour compter selon le rôle
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countNonLues(@PathVariable Long userId, @RequestParam String role) {
        return ResponseEntity.ok(notificationRepository.countByUserIdTargetAndRecipientRoleAndLuFalse(userId, role));
    }

    @PutMapping("/{id}/lire")
    public ResponseEntity<Void> marquerCommeLue(@PathVariable String id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setLu(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }
}