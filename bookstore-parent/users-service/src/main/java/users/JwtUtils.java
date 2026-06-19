package users;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtils {

    // En un entorno real, esta clave debe ser más segura y obtenida a través de variables de entorno o un gestor de secretos
    private static final String SECRET_KEY = "unir-supplies-secret-key-for-jwt-token-generation-and-validation-2025";
    private static final long ACCESS_TOKEN_VALIDITY = 5 * 60 * 1000; // 5 minutos
    private final SecretKey key;

    public JwtUtils() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Valida un token JWT comprobando su firma y fecha de expiración.
     * @param token - token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Genera un token JWT con el nombre de usuario y el ID de usuario como claims.
     * @param username - nombre de usuario
     * @param userId - ID de usuario
     * @return token JWT
     */
    public String generateAccessToken(String username, Integer userId) {
        return generateToken(username, userId);
    }

    /**
     * Extrae el userId del token JWT.
     * @param token - token JWT
     * @return userId o null si el token es inválido
     */
    public String getCifFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return String.valueOf(claims.get("cif", String.class));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Genera un token JWT con una validez específica.
     *
     * @param username - nombre de usuario
     * @param userId   - ID de usuario
     * @return token JWT
     */
    private String generateToken(String username, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JwtUtils.ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("cif", username) // Añadir el claim "cif" con el valor del nombre de usuario
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}