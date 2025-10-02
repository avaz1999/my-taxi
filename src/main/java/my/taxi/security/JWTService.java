package my.taxi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import my.taxi.security.web.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
@Service
@RequiredArgsConstructor
public class JWTService {
    private static final long CLOCK_SKEW_MILLIS = 2000L;
    private final JwtProperties properties;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecretBase64());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(properties.getIssuer())
                .setAllowedClockSkewSeconds(CLOCK_SKEW_MILLIS)
                .build();
    }

    public String issueAccess(Long userId, String subjectPhone, Set<String> roles, long tokenVersion) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = Map.of(
                "typ", "access",
                "uid", userId,
                "roles", roles,
                "ver", tokenVersion
        );
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject(subjectPhone)
                .setId(UUID.randomUUID().toString()) // jti
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + properties.getAccessExpMin() * 60_000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String issueRefresh(Long userId, String familyId, String jti, long tokenVersion) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = Map.of(
                "typ", "refresh",
                "uid", userId,
                "fid", familyId,
                "ver", tokenVersion
        );
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(properties.getIssuer())
                .setAudience(properties.getAudience())
                .setSubject(String.valueOf(userId))
                .setId(jti)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + properties.getRefreshExpDays() * 24L * 60L * 60L * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims verify(String token) {
        return parser().parseClaimsJws(token).getBody();
    }

    public boolean isExpired(String token) {
        try {
            Date exp = verify(token).getExpiration();
            return exp.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
