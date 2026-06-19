package users;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserJpaRepository userRepository;
    private final JwtUtils jwtUtils;

    /**
     * Obtiene el perfil del usuario autenticado.
     * El userId se obtiene del token JWT.
     * @param userId ID del usuario (path variable)
     * @param token Header de autorización con el token JWT
     * @return Perfil del usuario o error si no está autorizado
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId,
                                          @RequestHeader("accessToken") String token) {

        // Validar que el header Authorization esté presente y tenga el formato correcto
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de autorización requerido");
        }

        // Extraer el token del header (remover "Bearer ")
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        String tokenUserId = jwtUtils.getCifFromToken(token);
        // Verificar que el userId del token coincida con el path parameter
        if (!tokenUserId.equals(userId)) {
            log.warn("Intento de acceso no autorizado al perfil de usuario: {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado para acceder a este perfil");
        }

        // Buscar el usuario en la base de datos
        Optional<User> userOptional = userRepository.findByCif(String.valueOf(userId));
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        // Crear el DTO sin incluir información sensible como la contraseña
        UserDto userDto = new UserDto(
        	user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getCif(),
            user.getSector(),
            user.getEmployees(),
            user.getFoundedYear()
        );
        return ResponseEntity.ok(userDto);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> getUserByEmail(@RequestBody LoginByEmailRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        User user = userOptional.get();
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto userDto = new UserDto(
        	user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getCif(),
            user.getSector(),
            user.getEmployees(),
            user.getFoundedYear()
        );
        return ResponseEntity.ok(userDto);
    }
}