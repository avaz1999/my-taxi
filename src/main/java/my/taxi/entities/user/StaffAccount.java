package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;

import java.time.Instant;

/**
 * Created by Avaz Absamatov
 * Date: 9/12/2025
 */
@Entity
@Table(name = "staff_account")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StaffAccount extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_staff_accounts_user"))
    private User user;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "password_change_at")
    private Instant passwordChangeAt;
}
