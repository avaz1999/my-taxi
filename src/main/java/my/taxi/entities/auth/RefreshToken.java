package my.taxi.entities.auth;

import jakarta.persistence.*;
import lombok.*;
import my.taxi.entities.auth.enums.TokenStatus;

import java.time.Instant;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Entity
@Table(name = "REFRESH_TOKENS", indexes = {
        @Index(name = "idx_rt_user", columnList = "userId"),
        @Index(name = "idx_rt_jti", columnList = "jti", unique = true),
        @Index(name = "idx_rt_family", columnList = "familyId")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RefreshToken {
    /**
     * jti - "JWT ID": a unique identifier for one refresh token
     * Used to track this exact token in the database, detect reuse, and mark it USED/REVOKED.
     */
    @Id
    @Column(name = "JTI", length = 64)
    private String jti;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    /**
     * familyId — an identifier that groups all refresh tokens from the same device/session.
     * Each device gets its own familyId. When we revoke a family, only that device’s tokens stop working.
     */
    @Column(name = "FAMILY_ID", nullable = false)
    private String familyId;

    @Column(name = "TOKEN_HASH", length = 64)
    private String tokenHash;

    @Column(name = "DEVICE_FP", length = 128)
    private String deviceFp;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 16)
    private TokenStatus status;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    /**
     * userAgent — the client’s User-Agent string (browser/app info).
     * Stored for audit and anomaly detection (e.g., unusual device). Not trusted for security by itself.
     */
    @Column(name = "USER_AGENT", length = 256)
    private String userAgent;

    @Column(name = "ROTATED_AT")
    private Instant rotatedAt;

    @Column(name = "LAST_USED_AT")
    private Instant lastUsedAt;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == TokenStatus.ACTIVE && !isExpired();
    }

    public void touch() {
        this.lastUsedAt = Instant.now();
    }
}
