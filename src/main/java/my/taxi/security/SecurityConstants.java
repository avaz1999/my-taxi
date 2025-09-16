package my.taxi.security;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
public final class SecurityConstants {
    private SecurityConstants() {}

    public static final String[] PERMIT_ALL = {
            "/actuator/health",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/otp/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
}
