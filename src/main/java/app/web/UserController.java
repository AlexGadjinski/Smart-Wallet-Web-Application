package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID id) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);

        modelAndView.addObject("user", user);
        modelAndView.setViewName("profile-menu");
        return modelAndView;
    }
}
