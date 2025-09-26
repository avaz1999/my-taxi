package my.taxi.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Getter
@Setter
public class LoginRequest implements Serializable {
    @NotBlank(message = "{login.phone.notBlank}")
    private String phone;
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
