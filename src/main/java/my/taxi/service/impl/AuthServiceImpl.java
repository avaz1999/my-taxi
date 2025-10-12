package my.taxi.service.impl;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
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
import my.taxi.security.DeviceFingerPrService;
import my.taxi.security.JWTService;
import my.taxi.security.web.CookieHeader;
import my.taxi.security.web.HttpCookieUtil;
import my.taxi.security.web.JwtProperties;
import my.taxi.security.web.UserProfilePort;
import my.taxi.service.AuthService;
import my.taxi.utils.HashUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication/Token service (non-rotating refresh model).
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li><b>Login:</b> issue short-lived access JWT and ensure a device-bound refresh token exists.</li>
 *   <li><b>Refresh:</b> validate cookie refresh JWT, enforce device binding & tokenVersion,
 *       update audit fields (touch), and return a new access JWT <i>without</i> rotating refresh.</li>
 *   <li><b>Session cap:</b> optionally revoke the oldest ACTIVE session when max sessions is reached.</li>
 * </ul>
 *
 * <p>Security model:</p>
 * <ul>
 *   <li><b>Non-rotating RT:</b> minimizes DB growth; relies on device fingerprint, familyId, and global tokenVersion.</li>
 *   <li><b>Device binding:</b> refresh must originate from the same device fingerprint.</li>
 *   <li><b>Global invalidation:</b> access/refresh checked against user tokenVersion.</li>
 *   <li><b>Cookie hygiene:</b> HttpOnly, Secure, and appropriate SameSite for the refresh cookie.</li>
 * </ul>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Use transactional boundaries for login/refresh flows to keep state consistent.</li>
 *   <li>Consider throttling audit updates (touch) to reduce write load.</li>
 * </ul>
 */

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    // ==== Constants ====
    private static final String MSG_UNAUTHORIZED = "Unauthorized";
    private static final String HDR_USER_AGENT = "User-Agent";
    // JWT claim keys
    private static final String CLAIM_TYP = "typ";
    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_FID = "fid";
    private static final String CLAIM_VER = "ver";
    private static final String TYP_REFRESH = "refresh";

    private final UserProfilePort userProfilePort;
    private final DeviceFingerPrService deviceFingerPrService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties props;
    private final HttpCookieUtil cookies;
    private final UserRepository userRepository;
    private final JWTService jwt;


    /**
     * Authenticates a user by phone, issues an access token, and ensures a refresh token exists
     * for the current device (fingerprint). Reuses an existing ACTIVE refresh token for this device
     * or creates a new one if none exists (and enforces max-session policy).
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Find user by phone; fail fast if not found.</li>
     *   <li>Compute device fingerprint and look up an ACTIVE refresh token bound to this device.</li>
     *   <li>If present and not expired → reuse it (no rotation on login).</li>
     *   <li>Otherwise → (optionally) revoke the oldest ACTIVE session, then create a brand-new RT
     *       with a new familyId for this device.</li>
     *   <li>Always issue a fresh access token and set the refresh cookie.</li>
     * </ul>
     *
     * <h4>Notes</h4>
     * <ul>
     *   <li>Non-rotating on login avoids DB churn and keeps one row per device family.</li>
     *   <li>Reusing the same RT on login is safe when you also verify device fingerprint on refresh.</li>
     *   <li>Keep cookie flags: HttpOnly, Secure, and appropriate SameSite.</li>
     * </ul>
     */
    @Override
    @Transactional
    public Response<String> handleLogin(Request<LoginRequest> dto, HttpServletRequest request, HttpServletResponse res) {
        final LoginRequest params = dto.getParams();
        Optional<User> optUser = userRepository.findByPhone(params.getPhone());
        if (optUser.isEmpty()) return Response.fail(404, "User not found", HttpStatus.NOT_FOUND);

        User user = optUser.get();
        Instant now = Instant.now();
        // Bind the session to a device-scoped fingerprint (userId-scoped)
        String fp = deviceFingerPrService.userScopedFingerprint(request, user.getId());

        // Find an existing ACTIVE refresh token for this user+device
        Optional<RefreshToken> optActive = refreshTokenRepository.findActiveByUserAndDevice(user.getId(), fp);

        // Roles for access token payload
        Set<String> roles = getRoles(user);

        // Access token always (short-lived)
        long tokenVersion = userProfilePort.currentTokenVersion(user.getId());
        String access = jwt.issueAccess(user.getId(), user.getUsername(), roles, tokenVersion);

        String rawRefresh;
        if (optActive.isPresent() && !optActive.get().isExpired()) {
            // Reuse existing ACTIVE refresh token (no rotation on login)
            RefreshToken rt = optActive.get();
            rt.touch();
            rt.setUserAgent(request.getHeader(HDR_USER_AGENT));
            refreshTokenRepository.save(rt);

            // Re-issue the same logical refresh JWT (same jti/family)
            rawRefresh = jwt.issueRefresh(rt.getUserId(), rt.getFamilyId(), rt.getJti(), tokenVersion);
        } else {
            // Enforce max concurrent device sessions if configured
            long activeCount = refreshTokenRepository.countActiveByUser(user.getId());

            if (activeCount >= props.getMaxSession())
                revokeOldestActiveRefreshToken(user);

            // Create a brand-new refresh token for this device (new family for the device)
            String jti = UUID.randomUUID().toString();
            final String familyId = UUID.randomUUID().toString();
            rawRefresh = persistAndIssueRefresh(request, jti, familyId, user.getId(), tokenVersion, fp, now);
        }
        // Set/refresh the cookie holding the refresh token
        cookies.writeRefreshCookie(res, rawRefresh, refreshCookieMaxAgeSeconds());
        return Response.ok(access);
    }

    /**
     * Validates an incoming refresh token from the cookie and, if valid and ACTIVE for this device,
     * returns a new access token (without rotating the refresh token).
     *
     * <p>Security checks (order matters):</p>
     * <ol>
     *   <li>Cookie present → JWT verify (sig/exp/nbf/iat).</li>
     *   <li>{@code typ == "refresh"} claim.</li>
     *   <li>Token version matches current user tokenVersion (global invalidation).</li>
     *   <li>DB row by {@code jti} exists, {@code status == ACTIVE}, and not expired.</li>
     *   <li>Device fingerprint matches (same device context).</li>
     * </ol>
     * If any check fails → revoke the whole family, clear cookie, and return 401.
     *
     * <p>On success:</p>
     * <ul>
     *   <li>Touch the refresh token metadata (lastUsedAt / userAgent) and persist.</li>
     *   <li>Issue a fresh access token and optionally refresh the cookie Max-Age.</li>
     * </ul>
     */
    @Override
    public Response<String> handleRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String cookieName = props.getCookie().getRefreshName();
        String refreshToken = CookieHeader.read(request, cookieName);
        if (refreshToken == null || refreshToken.isBlank())
            return Response.fail(404, "Invalid refreshToken", HttpStatus.NOT_FOUND);

        Claims claims;
        try {
            claims = jwt.verify(refreshToken);
        } catch (Exception e) {
            return Response.fail(401, MSG_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!TYP_REFRESH.equals(claims.get(CLAIM_TYP, String.class)))
            return Response.fail(401, MSG_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);

        String jti = claims.getId();
        Long userId = claims.get(CLAIM_UID, Number.class).longValue();
        String familyId = claims.get(CLAIM_FID, String.class);
        long verFromToken = claims.get(CLAIM_VER, Number.class).longValue();

        // Global invalidation via tokenVersion mismatch
        final long currentVersion = userProfilePort.currentTokenVersion(userId);
        if (verFromToken != currentVersion)
            return revokeFamilyAndClearCookie(response, familyId);

        // Look up the refresh token row by jti
        RefreshToken currentRT = refreshTokenRepository.findByJti(jti).orElse(null);
        if (currentRT == null || currentRT.getStatus() != TokenStatus.ACTIVE || currentRT.isExpired())
            return revokeFamilyAndClearCookie(response, familyId);

        // Enforce device binding (fingerprint must match)
        String deviceFp = deviceFingerPrService.userScopedFingerprint(request, userId);
        if (!deviceFp.equals(currentRT.getDeviceFp()))
            return revokeFamilyAndClearCookie(response, familyId);

        // Non-rotating: only touch audit fields (you can throttle to reduce writes)
        currentRT.touch(); // updates lastUsedAt = now
        refreshTokenRepository.save(currentRT);

        // Issue a fresh access token
        final Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty())
            return revokeFamilyAndClearCookie(response, familyId);

        final User user = optUser.get();
        Set<String> roles = getRoles(user);
        final String newAccess = jwt.issueAccess(userId, user.getUsername(), roles, verFromToken);

        // Optionally renew the cookie Max-Age with the same refresh token
        cookies.writeRefreshCookie(response, refreshToken, refreshCookieMaxAgeSeconds());
        return Response.ok(newAccess);
    }

    /**
     * Get user roles
     */
    private static Set<String> getRoles(User user) {
        return user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());
    }

    /**
     * Issues a new refresh JWT and persists its metadata (only the hash) to the database.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Create a raw refresh JWT via {@link @jwt.issueRefresh(Long, String, String, long)}.</li>
     *   <li>Compute and store the SHA-256 hash of the raw token (never store raw tokens).</li>
     *   <li>Populate device/session context (familyId, deviceFp) and audit fields.</li>
     *   <li>Calculate {@code expiresAt} from the configured refresh lifetime.</li>
     * </ol>
     *
     * <h4>Why store the hash instead of the raw token?</h4>
     * If the database is leaked, the raw token cannot be replayed. On verification, you can hash the
     * presented token and compare against the stored hash.
     *
     * <h4>Security notes</h4>
     * <ul>
     *   <li>{@code familyId} ties all tokens of a single device/session; revoking the family
     *       invalidates all its tokens at once.</li>
     *   <li>{@code deviceFp} helps ensure the refresh is used from the same device context.</li>
     *   <li>When setting the cookie, use {@code HttpOnly}, {@code Secure}, and an appropriate
     *       {@code SameSite} attribute.</li>
     * </ul>
     *
     * @param request      HTTP request (used to capture User-Agent for audit)
     * @param jti          unique refresh token identifier (JWT ID)
     * @param familyId     logical “device/session family” identifier
     * @param userId       user identifier
     * @param tokenVersion global token version for bulk invalidation
     * @param deviceFp     device fingerprint bound to this refresh token
     * @param now          current timestamp (injected for testability/determinism)
     * @return the raw refresh token to send back to the client (e.g., set-cookie)
     * @implNote - {@link #refreshLifetime()} should read from configuration (e.g., days).
     * - Persist ONLY the token hash: {@code tokenHash = sha256(rawRefresh)}.
     * - In a non-rotating model, reuse the existing ACTIVE row; in a rotating model,
     * mark the old row and insert a new one.
     */
    private String persistAndIssueRefresh(HttpServletRequest request,
                                          String jti,
                                          String familyId,
                                          Long userId,
                                          long tokenVersion,
                                          String deviceFp,
                                          Instant now) {
        final String rawRefresh = jwt.issueRefresh(userId, familyId, jti, tokenVersion);

        final Instant expiresAt = now.plus(refreshLifetime());
        final RefreshToken entity = RefreshToken.builder()
                .jti(jti)
                .userId(userId)
                .familyId(familyId)
                .tokenHash(HashUtils.sha256Hex(rawRefresh))
                .deviceFp(deviceFp)
                .status(TokenStatus.ACTIVE)
                .expiresAt(expiresAt)
                .userAgent(request.getHeader(HDR_USER_AGENT))
                .lastUsedAt(now)
                .createdAt(now)
                .build();

        refreshTokenRepository.save(entity);
        return rawRefresh;
    }

    /**
     * Computes the cookie Max-Age (in seconds) for the refresh token cookie.
     *
     * <p>Derives from the configured refresh lifetime in days. The value must fit into a 32-bit
     * integer because the Set-Cookie Max-Age attribute is an integer. We use {@link Math#toIntExact(long)}
     * to fail-fast if the configured lifetime is unreasonably large.</p>
     *
     * <h4>Notes</h4>
     * <ul>
     *   <li>Max-Age should be ≤ the actual refresh token's JWT exp to avoid "zombie" cookies.</li>
     *   <li>Consider clamping (see alternative below) if you prefer not to throw.</li>
     * </ul>
     *
     * @return Max-Age in seconds to use on the refresh cookie.
     */
    private int refreshCookieMaxAgeSeconds() {
        // Convert configured days → seconds; throws if it overflows int
        return Math.toIntExact(Duration.ofDays(props.getRefreshExpDays()).getSeconds());
    }


    /**
     * Returns the logical lifetime of a refresh token as a {@link Duration}.
     *
     * <p>This value should match the exp claim used when issuing refresh JWTs so that
     * database checks (e.g., {@code expiresAt}) and cryptographic checks (JWT {@code exp})
     * remain consistent.</p>
     *
     * @implNote If you support per-tenant/per-user policies, consider passing the subject
     * (or policy object) to this method and computing a dynamic duration.
     */
    private Duration refreshLifetime() {
        return Duration.ofDays(props.getRefreshExpDays());
    }

    /**
     * Revokes the oldest ACTIVE refresh token for the given user, enforcing a "max sessions"
     * policy without logging the user out of all devices.
     *
     * <p>We fetch one oldest ACTIVE token (by created/lastUsed ordering in the repository query),
     * flip its status to {@code REVOKED}, and persist. This keeps DB churn minimal and preserves
     * other active sessions.</p>
     *
     * <h4>Transactional/Safety notes</h4>
     * <ul>
     *   <li>Call within a transactional boundary to avoid race conditions if multiple logins
     *       happen concurrently.</li>
     *   <li>Consider doing this via a single UPDATE query in the repository for fewer round-trips.</li>
     * </ul>
     *
     * @param user the subject whose oldest active session will be revoked
     */
    private void revokeOldestActiveRefreshToken(User user) {
        refreshTokenRepository.findActiveOldestFirst(user.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .ifPresent(old -> {
                    old.setStatus(TokenStatus.REVOKED);
                    refreshTokenRepository.save(old);
                });
    }


    /**
     * Revokes all refresh tokens in the given family and clears the client's refresh cookie.
     *
     * <p>Use this when a refresh validation fails (version mismatch, fingerprint mismatch,
     * revoked/expired token, etc.). It ensures the entire device/session “family” can no
     * longer be used and forces the client to log in again.</p>
     *
     * @param response HTTP response used to clear the refresh cookie
     * @param familyId logical device/session family identifier to revoke
     * @return 401 Unauthorized response payload
     * @implNote Call inside a transaction to ensure the revoke is atomic with any other
     * state changes. Keep this path idempotent (repeated calls are safe).
     */
    private Response<String> revokeFamilyAndClearCookie(HttpServletResponse response, String familyId) {
        revokeFamily(familyId);
        cookies.clearRefreshCookie(response);
        return Response.fail(401, MSG_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Marks all refresh tokens that belong to the given family as {@code REVOKED}.
     *
     * <p>This disables further use of any token in the same device/session family. Clients
     * will be unable to obtain new access tokens using those refresh tokens.</p>
     *
     * @param familyId family identifier whose tokens should be revoked
     * @implNote - Prefer executing within a transactional boundary.
     * - Consider a bulk UPDATE to reduce round-trips (see alternative below).
     * - Method is intentionally idempotent: calling it multiple times is safe.
     */
    private void revokeFamily(String familyId) {
        var list = refreshTokenRepository.findByFamilyId(familyId);
        list.forEach(r -> r.setStatus(TokenStatus.REVOKED));
        refreshTokenRepository.saveAll(list);
    }

}
