package app.web;

import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView getNotificationPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        NotificationPreference notificationPreference = notificationService.getNotificationPreference(user.getId());
        List<Notification> notifications = notificationService.getNotificationHistory(user.getId());
        long succeededNotificationsCount = notifications.stream().filter(n -> n.getStatus().equals("SUCCEEDED")).count();
        long failedNotificationsCount = notifications.stream().filter(n -> n.getStatus().equals("FAILED")).count();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("notifications");
        modelAndView.addObject("user", user);
        modelAndView.addObject("notificationPreference", notificationPreference);
        modelAndView.addObject("succeededNotificationsCount", succeededNotificationsCount);
        modelAndView.addObject("failedNotificationsCount", failedNotificationsCount);
        modelAndView.addObject("notifications", notifications.stream().limit(5).toList());

        return modelAndView;
    }

    @PatchMapping("/preferences")
    public String updatePreference(@RequestParam(name = "enabled") boolean isEnabled, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        notificationService.updatePreference(authenticationMetadata.getUserId(), isEnabled);

        return "redirect:/notifications";
    }

}
