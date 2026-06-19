package main;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @LoadBalanced
    private final WebClient.Builder webClientBuilder;

    /**
     * Valida un token opaco llamando al microservicio de usuarios.
     * Solo puede devolver 200 (válido) o error (inválido/expirado).
     */
    public Mono<SessionValidationResponse> validateToken(String tokenId) {
    	return webClientBuilder.build()
    	        .get()
    	        .uri("http://users-service/api/v1/tokens/{tokenId}", tokenId)  // ← cambiar supplies-users por users-service
    	        .retrieve()
    	        .onStatus(HttpStatus.GONE::equals, response -> {
    	            return Mono.error(new RuntimeException("Token is invalid or expired"));
    	        })
    	        .onStatus(status -> !status.is2xxSuccessful(), response -> {
    	            return Mono.error(new RuntimeException("Token validation failed"));
    	        })
    	        .bodyToMono(SessionValidationResponse.class)
    	        .doOnSuccess(response -> log.debug("Token validated successfully"))
    	        .doOnError(error -> log.warn("Token validation failed: {}", error.getMessage()));
    }
}