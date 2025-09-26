package my.taxi.controller;

import jakarta.validation.Valid;
import my.taxi.base.Request;
import my.taxi.base.Response;
import my.taxi.payload.request.LoginRequest;
import my.taxi.service.AuthService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Response<String>> login(@Valid @RequestBody Request<LoginRequest> request) {
        return ResponseEntity.ok(service.login(request));
    }
}
