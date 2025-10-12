package my.taxi.security;

import jakarta.servlet.http.HttpServletRequest;
import my.taxi.utils.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by Avaz Absamatov
 * Date: 10/10/2025
 */
@Service
public class DeviceFingerPrService {
    @Value("${security.hmacSecret}")
    private String hmacSecret;

    public String userScopedFingerprint(HttpServletRequest req, long userId) {
        String clientId = req.getHeader("X-Device-Id");
        if (clientId == null || clientId.isBlank())
            throw new IllegalArgumentException("Missing X-Device-Id header");

        String ua = normalizeUa(req.getHeader("User-Agent"));
        String data = clientId + ":" + userId + ":" + ua;  // user-scoped fingerprint
        return HashUtils.hmacSha256Hex(hmacSecret, data);
    }

    private String normalizeUa(String ua) {
        if (ua == null) return "NA";
        ua = ua.toLowerCase();
        return ua;
    }
}
