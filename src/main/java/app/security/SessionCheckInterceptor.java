package app.security;

import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    private static final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/", "/login", "/register");
    private static final Set<String> ADMIN_ENDPOINTS = Set.of("/users", "/reports");
    private final UserService userService;

    public SessionCheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    // изпълнява се преди всяка заявка
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String endpoint = request.getServletPath();

        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            return true;
        }

        // request.getSession() - взима сесията; ако няма такава, създава нова
        // request.getSession(false) - взима сесията; ако няма такава, връща null
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        if (!user.isActive()) {
            session.invalidate();
            response.sendRedirect("/");
            return false;
        }

//        НАЧИН 1:
//        if (ADMIN_ENDPOINTS.contains("/" + endpoint.split("/")[1]) && user.getRole() != Role.ADMIN) {
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            response.getWriter().write("Access denied, you don't have the necessary permissions!");
//            return false;
//        }

//        НАЧИН 2:
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.hasMethodAnnotation(RequireAdminRole.class) && user.getRole() != Role.ADMIN) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access denied, you don't have the necessary permissions!");
            return false;
        }

        return true;
    }
}
