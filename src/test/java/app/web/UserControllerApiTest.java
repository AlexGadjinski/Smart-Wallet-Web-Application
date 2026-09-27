package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.Role;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putUnauthorizedRequestToSwitchUserRoleEndpoint_shouldReturn404AndNotFoundView() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "Vik123", "123456", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
        verify(userService, never()).switchRole(any());
    }

    @Test
    void putAuthorizedRequestToSwitchUserRoleEndpoint_shouldRedirectToUsers() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "Vik123", "123456", Role.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).switchRole(userId);
    }
}
