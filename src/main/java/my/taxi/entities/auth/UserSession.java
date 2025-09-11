package my.taxi.entities.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;
import my.taxi.entities.auth.enums.PlatformType;
import my.taxi.entities.user.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "idx_user_sessions_user", columnList = "user_id"),
                @Index(name = "idx_user_sessions_revoked_at", columnList = "revoked_at"),
                @Index(name = "idx_user_sessions_user_revoked", columnList = "user_id, revoked")
        })
@SQLDelete(sql = "UPDATE user_sessions SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSession extends BaseEntity {
    /**
     * The user to whom this session belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_sessions_users"))
    private User user;

    /**
     * User sessionId (with cookie/header)
     */
    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    /**
     * RefreshTokeId
     */
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    /**
     * IP address of the client at the time the session was created.
     * Example: "192.168.1.15"
     */
    @Column(name = "ip", length = 64)
    private String ip;

    /**
     * The User-Agent string from the client request.
     * Contains information about the browser, operating system, and device type
     * used to create this session.
     * Example:
     * - "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
     * (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
     * - "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X)
     * AppleWebKit/605.1.15 Version/17.0 Mobile/15E148 Safari/604.1"
     * Mainly used for security auditing, monitoring active sessions,
     * and providing device-specific insights.
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * The platform or device type on which the session was created.
     * Unlike the full User-Agent string, this field stores only a simplified
     * classification such as WEB, ANDROID, or IOS.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "platform")
    private PlatformType platform;

    /**
     * Expiration timestamp of this session.
     * After this time the session is no longer valid.
     */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /**
     * Whether this session has been revoked.
     */
    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    /**
     * The time at which this session was revoked.
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * Reason for session revocation (manual logout, token reuse, etc.).
     */
    @Column(name = "revoke_reason")
    private String revokeReason;

    public void revoke(String reason) {
        this.revoked = true;
        this.revokedAt = Instant.now();
        this.revokeReason = reason;
    }

    public boolean isActive() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}
