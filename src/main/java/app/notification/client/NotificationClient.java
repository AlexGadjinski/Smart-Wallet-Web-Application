package app.notification.client;

import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.UpsertNotificationPreference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-svc", url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertNotificationPreference(@RequestBody UpsertNotificationPreference upsertNotificationPreference);

    @GetMapping("/preferences")
    ResponseEntity<NotificationPreference> getNotificationPreference(@RequestParam(name = "userId") UUID userId);

    /**
     * NOTE: This operation is conceptually a PATCH (partial update),
     * but due to limitations in the Feign client setup, PATCH does not work.
     * Using PUT here as a workaround.
     */
    @PutMapping("/preferences")
    ResponseEntity<Void> updateNotificationPreference(@RequestParam(name = "userId") UUID userId, @RequestParam(name = "enabled") boolean isEnabled);

    @PostMapping
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest);

    @GetMapping
    ResponseEntity<List<Notification>> getNotificationHistory(@RequestParam(name = "userId") UUID userId);
}
