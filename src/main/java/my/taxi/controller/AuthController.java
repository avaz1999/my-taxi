package my.taxi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.taxi.base.Request;
import my.taxi.base.Response;
import my.taxi.payload.request.LoginRequest;
import my.taxi.service.impl.AuthServiceImpl;
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
@Slf4j
@RestController
@RequestMapping(BASE_URL + AUTH)
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl service;


    @PostMapping(LOGIN)
    public ResponseEntity<Response<String>> login(
            @Valid @RequestBody Request<LoginRequest> dto,
            HttpServletRequest request,
            HttpServletResponse response) {
        Response<String> result = service.handleLogin(dto, request, response);
        log.info("Login successfully");
        return ResponseEntity.ok(result);
    }

    @PostMapping(REFRESH_TOKEN)
    public ResponseEntity<Response<String>> refreshToken(
            HttpServletRequest req,
            HttpServletResponse res) {
        return ResponseEntity.ok(service.handleRefreshToken(req, res));
    }
}
