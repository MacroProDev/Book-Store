package users;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSessionRepository {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final long TTL_SECONDS = 300; // 5 minutos

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Guarda una sesión en Redis con un TTL asignado.
     * @param redisSessionData - Datos de la sesión a guardar.
     */
    public void saveSession(RedisSessionData redisSessionData) {

        String key = SESSION_KEY_PREFIX + redisSessionData.getSessionId();
        try {
            String jsonValue = objectMapper.writeValueAsString(redisSessionData);
            redisTemplate.opsForValue().set(key, jsonValue, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar sesión en Redis", e);
        }
    }

    /**
     * Busca una sesión por su ID.
     * @param sessionId - ID de la sesión a buscar.
     * @return Optional con los datos de la sesión si existe y no ha expirado, vacío en caso contrario.
     */
    public Optional<RedisSessionData> findSession(String sessionId) {

        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            RedisSessionData redisSessionData = objectMapper.readValue(value.toString(), RedisSessionData.class);
            return Optional.of(redisSessionData);
        } catch (Exception e) {
            deleteSession(sessionId);
            return Optional.empty();
        }
    }

    /**
     * Elimina una sesión por su ID.
     * @param sessionId - ID de la sesión a eliminar.
     */
    public void deleteSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

}