package app.web;

import app.TestSetup;
import app.exception.UsernameAlreadyExistException;
import app.security.AuthenticationMetadata;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {
        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        // model().attributeExists("registerRequest") - проверява дали има конкретен not null атрибут
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("registerRequest", instanceOf(RegisterRequest.class)));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginRequest", instanceOf(LoginRequest.class)));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewWithErrorMessageAttribute() throws Exception {

        MockHttpServletRequestBuilder request = get("/login")
                .param("error", "error occurred");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginRequest", instanceOf(LoginRequest.class)))
                .andExpect(model().attribute("errorMessage", instanceOf(String.class)));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .formField("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void postRequestToRegisterEndpoint_whenUsernameAlreadyExists_shouldRedirectToRegisterWithFlashAttribute() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf());

        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exists!"));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void getAuthenticatedRequestToHomeEndpoint_shouldReturnHomeView() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "Vik123", "123456", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        when(userService.getById(userId)).thenReturn(TestSetup.aUser());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("user", instanceOf(User.class)));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    void getUnauthenticatedRequestToHomeEndpoint_shouldRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/home");

        // Изписваме пълния url, тъй като в случая Spring Security прави redirect-а
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        verify(userService, never()).getById(any());
    }

}
