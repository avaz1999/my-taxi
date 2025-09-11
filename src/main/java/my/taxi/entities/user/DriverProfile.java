package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.*;
import my.taxi.base.BaseEntity;
import my.taxi.entities.user.enums.DriverStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(name = "driver_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_driver_user", columnNames = {"user_id"}),
        indexes = {
                @Index(name = "idx_driver_user", columnList = "user_id"),
                @Index(name = "idx_driver_status", columnList = "status")
        })
@SQLDelete(sql = "UPDATE driver_profiles SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DriverProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_driver_profiles_user"))
    private User user;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private DriverStatus status;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    private Double ratingAvg;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @PrePersist
    public void prePersist() {
        if (status == null) this.status = DriverStatus.DRAFT;
        if (ratingAvg == null) ratingAvg = 0.0;
        if (ratingCount == null) ratingCount = 0;
    }
}
