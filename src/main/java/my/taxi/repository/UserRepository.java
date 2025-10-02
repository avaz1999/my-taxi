package my.taxi.repository;

import my.taxi.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
