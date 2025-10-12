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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
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
@SQLDelete(sql = "UPDATE users SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity implements UserDetails {
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

    /**
     * tokenVersion — simple way to invalidate ALL old tokens of a user.
     * <p>
     * How it works:
     * - We put the current tokenVersion into every issued JWT (claim: "ver").
     * - On each request, we compare JWT "ver" with the user's tokenVersion in DB.
     * - If they are different → the token is considered invalid (force re-login).
     * <p>
     * When to increment (bump) tokenVersion:
     * - User changes password.
     * - User roles/permissions change.
     * - User clicks "Logout from all devices".
     * - Security incident (suspicious login, etc.).
     * <p>
     * Why we need it:
     * - Without tokenVersion, old tokens stay valid until they expire.
     * - With tokenVersion, we can instantly revoke all existing tokens for that user.
     */

    @Column(name = "token_version")
    private long tokenVersion = 0L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
