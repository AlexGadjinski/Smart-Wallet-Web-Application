package app.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MyAPIController {

    @GetMapping("/info/1")
    public String getInfo1(HttpServletRequest request, HttpServletResponse response) {
        String author = request.getHeader("Author");
        response.addHeader("Application-Name", "Spring Boot App, Smart Wallet!");

        Cookie cookie = new Cookie("color", "Blue");
        response.addCookie(cookie);

        return "Hello, " + author + "!";
    }


    @GetMapping("/info/2")
    public String getInfo2(@CookieValue(value = "color", defaultValue = "Blue") String color,
                           @CookieValue(value = "city", defaultValue = "Sofia") String city,
                           @CookieValue(value = "country", defaultValue = "Bulgaria") String country) {
        return "%s %s %s".formatted(color, city, country);
    }
}
