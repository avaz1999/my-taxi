package my.taxi.repository;

import my.taxi.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByPhone(String phone);
}
