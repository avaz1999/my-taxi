package my.taxi.entities.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {
    @Id
    @Column(name = "jti", length = 64)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
