package my.taxi.security.web;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
public interface UserProfilePort {
    Long findUserIdByPhone(String phone);
    long currentTokenVersion(Long userId);
}
