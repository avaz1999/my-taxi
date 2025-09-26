package my.taxi.repository;

import my.taxi.entities.user.StaffAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Avaz Absamatov
 * Date: 9/24/2025
 */
public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {
}
