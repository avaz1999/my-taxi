package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;
import my.taxi.entities.user.enums.Language;
import my.taxi.entities.user.enums.Role;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.*;

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
@SQLDelete(sql = "UPDATE users SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
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
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Language language = Language.UZ_LATN;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    @Column(name = "active", nullable = false)
    private boolean active;


}
