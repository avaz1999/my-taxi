package my.taxi.security.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
@Component
@RequiredArgsConstructor
public class HttpCookieUtil {
    private final JwtProperties props;

    public void writeRefreshCookie(HttpServletResponse res, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(props.getCookie().getRefreshName(), value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);

        res.setHeader("Set-Cookie", cookieToString(cookie, props.getCookie().getSameSite()));
    }

    public void clearRefreshCookie(HttpServletResponse res) {
        Cookie c = new Cookie(props.getCookie().getRefreshName(), "");
        c.setHttpOnly(true);
        c.setSecure(true);
        c.setPath("/");
        c.setMaxAge(0);
        res.addHeader("Set-Cookie", cookieToString(c, props.getCookie().getSameSite()));
    }

    private static String cookieToString(Cookie c, String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getName()).append("=").append(c.getValue()).append("; Path=").append(c.getPath());
        sb.append("; Max-Age=").append(c.getMaxAge());
        sb.append("; HttpOnly");
        sb.append("; Secure");
        if (sameSite != null) sb.append("; SameSite=").append(sameSite);
        return sb.toString();
    }
}
