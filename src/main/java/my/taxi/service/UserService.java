package my.taxi.service;

import my.taxi.entities.user.User;
import my.taxi.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return null;
    }
}
