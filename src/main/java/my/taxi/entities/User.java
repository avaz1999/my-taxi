package my.taxi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;

/**
 * Created by: Avaz Absamatov
 * 08.09.2025
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_phone", columnList = "phone")
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {
    @Column(name = "phone", unique = true, nullable = false)
    private String phone;


}
