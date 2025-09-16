package my.taxi.service;

import my.taxi.entities.user.User;
import my.taxi.payload.request.LoginRequest;
import my.taxi.repository.UserRepository;
import my.taxi.security.JwtService;
import org.springframework.stereotype.Service;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone());
        if (user == null) {
            return "Invalid phone or password";
        }
        return null;
    }
}
