package my.taxi.controller;

import my.taxi.payload.request.LoginRequest;
import my.taxi.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static my.taxi.utils.ApiConstants.*;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@RestController
@RequestMapping(BASE_URL + AUTH)
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping(LOGIN)
    public String login(@RequestBody LoginRequest request) {
        return service.login(request);
    }
}
