package my.taxi.repository;

import my.taxi.entities.user.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Avaz Absamatov
 * Date: 9/24/2025
 */
public interface StaffProfileRepository extends JpaRepository<StaffProfile,Long> {
}
