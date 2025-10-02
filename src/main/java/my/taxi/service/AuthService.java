package my.taxi.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import my.taxi.base.Request;
import my.taxi.base.Response;
import my.taxi.entities.auth.RefreshToken;
import my.taxi.entities.auth.enums.TokenStatus;
import my.taxi.entities.user.User;
import my.taxi.entities.user.enums.Role;
import my.taxi.payload.request.LoginRequest;
import my.taxi.repository.RefreshTokenRepository;
import my.taxi.repository.UserRepository;
import my.taxi.security.JWTService;
import my.taxi.security.web.CookieHeader;
import my.taxi.security.web.HttpCookieUtil;
import my.taxi.security.web.JwtProperties;
import my.taxi.security.web.UserProfilePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserProfilePort userProfilePort;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties props;
    private final HttpCookieUtil cookies;
    private final UserRepository userRepository;
    private final JWTService jwt;


    public Response<String> handleLogin(Request<LoginRequest> dto, HttpServletRequest request, HttpServletResponse res) {
        LoginRequest params = dto.getParams();
        User user = userRepository.findByPhone(params.getPhone()).orElse(null);
        if (user == null) return Response.fail(404, "User not found", HttpStatus.NOT_FOUND);

        Long userId = userProfilePort.findUserIdByPhone(user.getUsername());
        long tokenVersion = userProfilePort.currentTokenVersion(userId);
        Set<String> roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        String familyId = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();
        String userAgent = request.getHeader("User-Agent");

        RefreshToken rt = RefreshToken.builder()
                .userId(userId)
                .jti(jti)
                .familyId(familyId)
                .status(TokenStatus.ACTIVE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(props.getRefreshExpDays() * 24L * 60L * 60L))
                .userAgent(userAgent)
                .build();
        refreshTokenRepository.save(rt);

        String access = jwt.issueAccess(userId, user.getUsername(), roles, tokenVersion);
        String refresh = jwt.issueRefresh(userId, familyId, jti, tokenVersion);


        int maxAge = (int) (props.getRefreshExpDays() * 24L * 60L * 60L);
        cookies.writeRefreshCookie(res, refresh, maxAge);
        return Response.ok(access);
    }

    public ResponseEntity<Response<String>> handleRefresh(HttpServletRequest req, HttpServletResponse res) {
        String cookieName = props.getCookie().getRefreshName();
        String read = CookieHeader.read(req, cookieName);
        if (read == null || read.isBlank()) return ResponseEntity.status(401).build();

        Claims claims;
        try {
            claims = jwt.verify(read);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        if (!"refresh".equals(claims.get("typ", String.class))) {
            return ResponseEntity.status(401).build();
        }
        String jti = claims.getId();
        Long userId = claims.get("uid", Number.class).longValue();
        String familyId = claims.get("fid", String.class);
        long version = claims.get("ver", Number.class).longValue();

        RefreshToken current = refreshTokenRepository.findByJti(jti).orElse(null);
        if (current == null || current.getStatus() != TokenStatus.ACTIVE || current.isExpired()) {
            revokeFamily(familyId);
            cookies.clearRefreshCookie(res);
            return ResponseEntity.status(401).build();
        }

        current.setStatus(TokenStatus.USED);
        current.setRotatedAt(Instant.now());
        current.setUserAgent(req.getHeader("User-Agent"));
        refreshTokenRepository.save(current);

        String newJti = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken
                .builder()
                .userId(userId)
                .jti(newJti)
                .familyId(familyId)
                .status(TokenStatus.ACTIVE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(props.getRefreshExpDays() * 24L * 60L * 60L))
                .build();

        refreshTokenRepository.save(refreshToken);

        String access = jwt.issueAccess(userId, String.valueOf(userId), Set.of(), version);
        int maxAge = (int) (props.getRefreshExpDays() * 24L * 60L * 60L);
        String newRefresh = jwt.issueRefresh(userId, familyId, newJti, version);
        cookies.writeRefreshCookie(res, newRefresh, maxAge);
        return ResponseEntity.ok(Response.ok(access));
    }

    private void revokeFamily(String familyId) {
        List<RefreshToken> list = refreshTokenRepository.findByFamilyId(familyId);
        for (RefreshToken r : list) r.setStatus(TokenStatus.REVOKED);
        refreshTokenRepository.saveAll(list);
    }

}
