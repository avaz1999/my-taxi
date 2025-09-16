package my.taxi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Created by Avaz Absamatov
 * Date: 9/13/2025
 */
@Slf4j
@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long accessExpMillis;
    private final long refreshExpMillis;
    private final String issuer;
    private final String audience;
    private final long clockSkewSec;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-exp-min}") long accessExpMin,
            @Value("${security.jwt.refresh-exp-days}") long refreshExpDays,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.audience}") String audience,
            @Value("${security.jwt.clock-skew-sec}") long clockSkewSec
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpMillis = accessExpMin * 60_000L;
        this.refreshExpMillis = refreshExpDays * 24L * 60L * 60L * 1000L;
        this.issuer = issuer;
        this.audience = audience;
        this.clockSkewSec = clockSkewSec;
    }


}
