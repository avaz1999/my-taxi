package my.taxi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.taxi.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static my.taxi.utils.BasePath.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private static final String REFRESH_PATH = BASE_URL + AUTH + REFRESH_TOKEN;
    private static final String BEARER = "Bearer ";
    private static final String INVALID_TOKEN = "Invalid token";
    private static final String ATTR_ACCESS_TOKEN = "accessToken";
    private static final String ATTR_EXPIRED_CLAIMS = "expiredJwtClaims";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final JWTService jwtService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException, ServletException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (header == null || !header.startsWith(BEARER)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(BEARER.length());

            String phone = jwtService.extractPhone(token);
            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = authService.loadByPhone(phone);

                if (jwtService.isTokenValid(token, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    writeJsonError(response, request,
                            HttpStatus.UNAUTHORIZED, INVALID_TOKEN, "JWT token is invalid");
                    return;
                }
                request.setAttribute(ATTR_ACCESS_TOKEN, token);

                filterChain.doFilter(request, response);
            }


            writeJsonError(response, request, HttpStatus.UNAUTHORIZED,
                    INVALID_TOKEN, "JWT token is invalid");

        } catch (ExpiredJwtException e) {
            if (PATH_MATCHER.match(REFRESH_PATH, request.getRequestURI())) {
                request.setAttribute(ATTR_EXPIRED_CLAIMS, e.getClaims());
                filterChain.doFilter(request, response);
            } else {
                SecurityContextHolder.clearContext();
                writeJsonError(response, request, HttpStatus.UNAUTHORIZED,
                        "token_expired", "JWT token expired");
            }
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // 401 – Token signature error
            SecurityContextHolder.clearContext();
            writeJsonError(response, request, HttpStatus.UNAUTHORIZED,
                    INVALID_TOKEN, "JWT token is malformed or unsupported");
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.warn("Auth unexpected error: method={}, uri={}, ip={}, reason={}",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr(),
                    e.getClass().getSimpleName());
            writeJsonError(response, request, HttpStatus.INTERNAL_SERVER_ERROR,
                    "server_error", "Unexpected authentication error");
        }
    }

    private void writeJsonError(HttpServletResponse response,
                                HttpServletRequest request,
                                HttpStatus status,
                                String error,
                                String description) throws IOException {
        if (response.isCommitted()) return;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "error", error,
                "error_description", description,
                "status", status.value(),
                "timestamp", Instant.now().toString(),
                "path", request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
