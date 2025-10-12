package my.taxi.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.taxi.base.Request;
import my.taxi.base.Response;
import my.taxi.payload.request.LoginRequest;

/**
 * Created by Avaz Absamatov
 * Date: 10/11/2025
 */
public interface AuthService {
    Response<String> handleLogin(Request<LoginRequest> dto, HttpServletRequest request, HttpServletResponse res);

    Response<String> handleRefreshToken(HttpServletRequest request, HttpServletResponse res);
}
