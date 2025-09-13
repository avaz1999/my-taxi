package my.taxi.entities.auth.enums;

/**
 * Created by Avaz Absamatov
 * Date: 9/13/2025
 */
public enum BrutScope {
    LOGIN,          // username/password
    OTP_SMS,        // sms-otp
    TOTP,           // 2FA
    PASSWORD_RESET  // forgot/reset
}
