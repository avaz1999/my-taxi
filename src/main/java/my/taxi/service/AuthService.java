package my.taxi.service;

import my.taxi.base.Request;
import my.taxi.base.Response;
import my.taxi.entities.user.User;
import my.taxi.payload.request.LoginRequest;
import my.taxi.repository.UserRepository;
import my.taxi.security.JWTService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JWTService jwtService;

    public AuthService(UserRepository userRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public Response<String> login(Request<LoginRequest> request) {
        LoginRequest params = request.getParams();
        User user = userRepository.findByPhone(params.getPhone());
        if (user == null) {
            return Response.fail(404, "User not found", HttpStatus.NOT_FOUND);
        }
        return Response.ok(jwtService.generateAccessToken(user));
    }

    public UserDetails loadByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
}
