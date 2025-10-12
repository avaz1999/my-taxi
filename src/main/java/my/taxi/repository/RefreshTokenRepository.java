package my.taxi.repository;

import my.taxi.entities.auth.RefreshToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    List<RefreshToken> findByFamilyId(String familyId);

    @Query("""
              SELECT r FROM RefreshToken r
              WHERE r.userId=:userId AND r.deviceFp=:deviceFp AND r.status = 'ACTIVE'
            """)
    Optional<RefreshToken> findActiveByUserAndDevice(@Param("userId") Long userId,
                                                     @Param("deviceFp") String deviceFp);

    @Query("""
              SELECT count(r) FROM RefreshToken r
              WHERE r.userId=:userId AND r.status='ACTIVE'
            """)
    long countActiveByUser(@Param("userId") Long userId);

    @Query("""
      SELECT r FROM RefreshToken r
      WHERE r.userId=:userId AND r.status='ACTIVE'
      ORDER BY coalesce(r.lastUsedAt, r.createdAt) ASC
    """)
    List<RefreshToken> findActiveOldestFirst(@Param("userId") Long userId, Pageable pageable);

}
