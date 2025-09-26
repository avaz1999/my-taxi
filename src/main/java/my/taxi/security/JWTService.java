package my.taxi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import my.taxi.entities.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by Avaz Absamatov
 * Date: 9/23/2025
 */
@Service
@Slf4j
public class JWTService {
    @Value("${application.security.jwt.secret-key}")
    private String secret;
    @Value("${application.security.jwt.expiration}")
    private Long expirationAccessTokenExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;
    @Value("${application.security.jwt.issuer}")
    private String issuer;

    private static final String PHONE = "phone";
    private static final String USER_ID = "userId";
    private static final String ROLES = "roles";
    private static final long CLOCK_SKEW = 60L * 1000L;


    public String extractPhone(String token) {
        Claims claims = extractClaims(token);
        return claims.get(PHONE, String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSiginingKey())
                .build()
                .parseClaimsJws(token).getBody();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date(System.currentTimeMillis() + CLOCK_SKEW));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String phone = extractPhone(token);
        return (Objects.equals(phone, userDetails.getUsername()) && isTokenExpired(token));
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        Map<String, Object> claims = Map.of(
                PHONE, user.getPhone(),
                USER_ID, user.getId(),
                ROLES, roles
        );
        return createToken(claims, user, expirationAccessTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, User user, Long expiration) {
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(user.getPhone())
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSiginingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractClaims(token);
        return claimResolver.apply(claims);
    }

    private Key getSiginingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
