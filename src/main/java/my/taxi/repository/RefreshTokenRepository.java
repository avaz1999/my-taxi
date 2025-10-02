package my.taxi.repository;

import my.taxi.entities.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);
    List<RefreshToken> findByFamilyId(String familyId);
}
