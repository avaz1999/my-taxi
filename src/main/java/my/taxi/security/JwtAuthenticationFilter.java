package my.taxi.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = auth.substring(7);

        Claims claims;
        try {
            claims = jwt.verify(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!"access".equals(claims.get("typ", String.class))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String phone = claims.getSubject();
        Set<String> roles = (Set<String>) claims.get("roles");

        var principal = User.withUsername(phone)
                .password("") // parol kerak emas (allaqachon tekshirilgan)
                .authorities((GrantedAuthority) (roles == null ? Set.of() : roles.toArray(String[]::new)))
                .build();

        var authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);


        chain.doFilter(request, response);
    }
}
