package app.notification.service;

import app.notification.client.NotificationClient;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.UpsertNotificationPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {
    private final NotificationClient notificationClient;

    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void saveNotificationPreference(UUID userId, boolean isEmailEnabled, String email) {

        UpsertNotificationPreference preference = UpsertNotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(isEmailEnabled)
                .type("EMAIL")
                .contactInfo(email)
                .build();

        ResponseEntity<Void> response = notificationClient.upsertNotificationPreference(preference);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Can't save user preference for user with id = [%s].".formatted(userId));
        }
    }

    public NotificationPreference getNotificationPreference(UUID userId) {

        ResponseEntity<NotificationPreference> response = notificationClient.getNotificationPreference(userId);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Notification preference for user with id = [%s] does not exist.".formatted(userId));
        }

        return response.getBody();
    }

    public void updatePreference(UUID userId, boolean isEnabled) {

        try {
            notificationClient.updateNotificationPreference(userId, isEnabled);
        } catch (Exception e) {
            log.warn("Can't update notification preference for user with id = [%s].".formatted(userId));
        }
    }

    public void sendNotification(UUID userId, String subject, String body) {

        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(body)
                .build();

        try {
            ResponseEntity<Void> response = notificationClient.sendNotification(notification);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Can't send notification to user with id = [%s].".formatted(userId));
            }

        } catch (Exception e) {
            log.warn("Can't send notification to user with id = [%s] due to 500 Internal Server Error.".formatted(userId));
        }
    }

    public List<Notification> getNotificationHistory(UUID userId) {

        ResponseEntity<List<Notification>> response = notificationClient.getNotificationHistory(userId);

        return response.getBody();
    }
}
