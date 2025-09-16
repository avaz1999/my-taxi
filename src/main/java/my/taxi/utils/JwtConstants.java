package my.taxi.utils;

import lombok.Getter;

/**
 * Created by: Avaz Absamatov
 * 15.09.2025
 */
@Getter
public enum JwtConstants {
    CODE("id"),
    TYP("typ"),
    ROLES("roles"),
    ISSUER("my_taxi_app");

    private final String value;

    JwtConstants(String value) {
        this.value = value;
    }
}
