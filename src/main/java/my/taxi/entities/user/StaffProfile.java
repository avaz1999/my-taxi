package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.*;
import my.taxi.base.BaseEntity;
import my.taxi.entities.user.enums.Role;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(name = "staf_proffiles",
        indexes = @Index(name = "idx_staff_user", columnList = "user_id"))
@SQLDelete(sql = "UPDATE staff_profiles SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_staff_profiles_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", nullable = false, length = 24)
    private Role staffRole; // OPERATOR / ADMIN / MANAGER

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;
}
