package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final UserService userService;

    public SubscriptionController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getUpgradePage() {
        return "upgrade";
    }

    @GetMapping("/history")
    public ModelAndView getUserSubscriptions() {
        User user = userService.getById(UUID.fromString("e637f917-e02d-4a8d-9682-6da71dc69b12"));
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("subscription-history");
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
