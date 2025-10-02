package my.taxi.security.web;

import lombok.RequiredArgsConstructor;
import my.taxi.entities.user.User;
import my.taxi.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Created by Avaz Absamatov
 * Date: 10/1/2025
 */
@Component
@RequiredArgsConstructor
public class UserProfileAdapter implements UserProfilePort{
    private final UserRepository repo;
    @Override
    public Long findUserIdByPhone(String phone) {
        return repo.findByPhone(phone)
                .map(User::getId)
                .orElseThrow();
    }

    @Override
    public long currentTokenVersion(Long userId) {
        return repo.findById(userId)
                .map(User::getTokenVersion)
                .orElse(0L);
    }
}
