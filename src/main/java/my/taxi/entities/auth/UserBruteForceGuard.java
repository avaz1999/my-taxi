package my.taxi.entities.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;
import my.taxi.entities.auth.enums.BrutScope;
import my.taxi.entities.user.User;

import java.time.Instant;

/**
 * Created by Avaz Absamatov
 * Date: 9/13/2025
 */
@Entity
@Table(name = "user_brute_force_guard")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserBruteForceGuard extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_brute_force_guard_users"))
    private User user;

    @Enumerated(EnumType.STRING)
    private BrutScope scope;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "windows_started_at")
    private Instant windowsStartedAt;

    @Column(name = "last_failed_at")
    private Instant lastFailedAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "strike", nullable = false)
    private short strike;

    @Column(name = "threshold", nullable = false)
    private int threshold;

    @Column(name = "window_sec", nullable = false)
    private int windowSec;

    @Column(name = "lock1_sec", nullable = false)
    private long lock1Sec;

    @Column(name = "lock2_sec", nullable = false)
    private long lock2Sec;

    @Column(name = "last_ip", length = 64)
    private String lastIP;

    @Column(name = "last_user_agent", length = 250)
    private String lastUserAgent;

    public boolean isLockedNow() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public void registrationSuccess() {
        strike = 0;
        windowsStartedAt = null;
        lockedUntil = null;
    }

    public void registerFailure() {
        final Instant now = Instant.now();
        if (windowsStartedAt == null || now.isAfter(windowsStartedAt.plusSeconds(windowSec))) {
            windowsStartedAt = now;
            strike = 1;
        } else strike++;

        if (strike >= threshold) {
            final boolean sever = strike >= (threshold * 2);
            long lock = sever ? lock2Sec : lock1Sec;
            lockedUntil = now.plusSeconds(lock);

            strike = 0;
            windowsStartedAt = null;
        }
    }

    @PrePersist
    public void defaults() {
        if (threshold <= 0) threshold = 7;
        if (windowSec <= 0) windowSec = 600;     // 10m
        if (lock1Sec <= 0) lock1Sec = 900;     // 15m
        if (lock2Sec <= 0) lock2Sec = 86400;   // 24h
        if (strike < 0) strike = 0;
        if (failedAttempts < 0) failedAttempts = 0;
    }
}
