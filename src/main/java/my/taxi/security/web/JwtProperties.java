package my.taxi.security.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String issuer;
    private String audience;
    private String secretBase64;
    private long accessExpMin;
    private long refreshExpDays;

    private CookieProps cookie = new CookieProps();


    @Getter
    @Setter
    public static class CookieProps {
        private String refreshName = "refresh_token";
        private String sameSite = "Strict";
    }
}
