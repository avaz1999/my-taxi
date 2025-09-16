package my.taxi.payload.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Getter
@Setter
public class LoginRequest {
    private String phone;
    private String password;
}
