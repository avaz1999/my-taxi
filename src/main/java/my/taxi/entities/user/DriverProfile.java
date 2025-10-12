package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.*;
import my.taxi.base.BaseEntity;
import my.taxi.entities.user.enums.DriverStatus;

import java.math.BigDecimal;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(name = "DRIVER_PROFILES",
        uniqueConstraints = @UniqueConstraint(name = "uk_driver_user", columnNames = {"USER_ID"}),
        indexes = {
                @Index(name = "idx_driver_user", columnList = "userId"),
                @Index(name = "idx_driver_status", columnList = "status")
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DriverProfile extends BaseEntity {
    @Column(name = "USER_ID")
    private Long user;

    @Column(name = "FIRST_NAME", nullable = false, length = 64)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false, length = 64)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 24)
    private DriverStatus status;

    @Column(name = "BALANCE", precision = 19, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "RATING_AVG", precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Column(name = "RATING_COUNT")
    private Integer ratingCount;

    @PrePersist
    public void prePersist() {
        if (status == null) this.status = DriverStatus.DRAFT;
        if (ratingAvg == null) ratingAvg = BigDecimal.valueOf(0.0);
        if (ratingCount == null) ratingCount = 0;
    }
}
