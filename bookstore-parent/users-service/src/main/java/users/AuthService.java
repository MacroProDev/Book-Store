package users;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final RedisSessionRepository redisSessionRepository;
    private final JwtUtils jwtUtils;


    /**
     * Autentica a un usuario y crea una sesión con tokens JWT.
     * @param username - CIF del usuario
     * @param password - Contraseña del usuario
     * @return ID de sesión si la autenticación es exitosa, vacío en caso contrario
     */
    public Optional<String> createPhantomToken(String username, String password) {

        Optional<User> userOptional = userRepository.findByCif(username); //Podria hacerse contra un LDAP
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        if (!user.getPassword().equals(password)) {
            return Optional.empty();
        }

        // Generar token JWT con duración de 5 minutos
        String accessToken = jwtUtils.generateAccessToken(username, user.getId());

        // Generar ID de sesión único (token opaco)
        String opaqueToken = UUID.randomUUID().toString();

        // Guardar sesión en Redis con TTL automático de 5 minutos
        RedisSessionData redisSessionData = new RedisSessionData(opaqueToken, accessToken, username, user.getId());
        redisSessionRepository.saveSession(redisSessionData);
        return Optional.of(opaqueToken);
    }

    /**
     * Valida una sesión existente.
     * @param opaqueToken - ID de la sesión a validar
     * @return Estado de la sesión y datos asociados (200 o 410 únicamente)
     */
    public SessionValidationResponse validatePhantomToken(String opaqueToken) {

        Optional<RedisSessionData> sessionOptional = redisSessionRepository.findSession(opaqueToken);
        if (sessionOptional.isEmpty()) {
            return new SessionValidationResponse(SessionStatus.NOT_FOUND, null);
        }
        RedisSessionData session = sessionOptional.get();

        // Verificar token de acceso
        if (jwtUtils.validateToken(session.getAccessToken())) {
            return new SessionValidationResponse(SessionStatus.VALID, session);
        }

        // Si el token no es válido, eliminar la sesión y retornar expirada
        redisSessionRepository.deleteSession(opaqueToken);
        return new SessionValidationResponse(SessionStatus.EXPIRED, null);
    }

    /**
     * Refresca una sesión existente generando nuevos tokens.
     * Solo funciona si el JWT actual sigue siendo válido.
     * @param opaqueToken - ID de la sesión actual
     * @return Par de nuevo token opaco y access token si la sesión es válida, vacío en caso contrario
     */
    public Optional<String> refreshPhantomToken(String opaqueToken) {

        Optional<RedisSessionData> sessionOptional = redisSessionRepository.findSession(opaqueToken);
        if (sessionOptional.isEmpty()) {
            return Optional.empty();
        }
        RedisSessionData currentSession = sessionOptional.get();

        // Verificar que el JWT actual sea válido para permitir el refresh
        if (!jwtUtils.validateToken(currentSession.getAccessToken())) {
            // Si el JWT no es válido, eliminar la sesión
            redisSessionRepository.deleteSession(opaqueToken);
            return Optional.empty();
        }

        // Generar nuevo token JWT con duración de 5 minutos
        String newAccessToken = jwtUtils.generateAccessToken(currentSession.getUsername(), currentSession.getUserId());

        // Generar nuevo token opaco para el cliente
        String newOpaqueToken = UUID.randomUUID().toString();

        // Eliminar la sesión actual
        redisSessionRepository.deleteSession(opaqueToken);

        // Crear una nueva sesión con nuevos tokens
        RedisSessionData newSession = new RedisSessionData(
                newOpaqueToken, newAccessToken,
                currentSession.getUsername(), currentSession.getUserId());

        redisSessionRepository.saveSession(newSession);
        // Retornar tanto el nuevo token opaco como el access token
        return Optional.of(newOpaqueToken);
    }
}