package app.notification.client.dto;

import lombok.Data;

@Data
public class NotificationPreference {

    private String type;

    private boolean notificationEnabled;

    private String contactInfo;
}
