package orders;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtils {

    private static final String SECRET_KEY = "unir-supplies-secret-key-for-jwt-token-generation-and-validation-2025";
    private final SecretKey key;

    public JwtUtils() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}