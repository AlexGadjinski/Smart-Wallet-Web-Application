package app.user.service;

import app.user.model.Country;
import app.web.dto.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserInit implements CommandLineRunner {
    private final UserService userService;

    public UserInit(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Vik123")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        userService.register(registerRequest);
    }
}
