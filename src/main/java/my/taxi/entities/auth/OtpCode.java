package my.taxi.entities.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.entities.auth.enums.OtpStatus;

import java.time.Instant;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(name = "otp_codes",
        indexes = {
                @Index(name = "idx_otp_phone_status", columnList = "phone, status"),
                @Index(name = "idx_otp_expires", columnList = "expires_at")
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OtpCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", length = 12)
    private String phone;

    @Column(name = "code_hash")
    private String codeHash;

    @Column(name = "salt")
    private String salt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "attempts")
    private short attempts;

    @Column(name = "max_attempts")
    private short maxAttempts;

    @Column(name = "resend_count")
    private short resendCount;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private OtpStatus status;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "created_at")
    private Instant createdAt;
}
