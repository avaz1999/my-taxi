package my.taxi.repository;

import my.taxi.entities.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.jti = :jti")
    void revoke(@Param("jti") String jti);
}
