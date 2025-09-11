package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;
import my.taxi.entities.user.enums.Role;

import java.util.Set;

/**
 * Created by: Avaz Absamatov
 * 08.09.2025
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_phone", columnList = "phone"),
                @Index(name = "idx_users_blocked", columnList = "blocked")
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {
    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_roles_user")))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 24)
    private Set<Role> roles;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;


}
