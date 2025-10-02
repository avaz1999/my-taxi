package my.taxi.security.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
@Component
public class CookieHeader {
    private CookieHeader() {
    }

    public static String read(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) if (cookie.getName().equals(name)) return cookie.getValue();
        return null;
    }
}
