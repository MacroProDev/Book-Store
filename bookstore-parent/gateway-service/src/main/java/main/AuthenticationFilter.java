package main;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        log.debug("Requested access to URI: {}", path);

        if (isPublicEndpoint(path, request) || request.getHeaders().getFirst("Stripe-Signature") != null) {
            log.debug("Access granted - public endpoint: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Access Denied - missing or invalid Authorization header");
            return respondWithError(exchange, HttpStatus.FORBIDDEN, "Authorization header missing or invalid");
        }

        String sessionId = authHeader.substring(7);

        return authService.validateToken(sessionId)
                .flatMap(tokenResponse -> {
                    log.debug("Valid session received for sessionId: {}", sessionId);
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("accessToken", tokenResponse.getAccessToken())
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(error -> {
                    log.warn("Invalid or expired token for sessionId: {}", sessionId);
                    return respondWithError(exchange, HttpStatus.UNAUTHORIZED, "Token invalid or expired");
                });
    }

    private boolean isPublicEndpoint(String path, ServerHttpRequest request) {
        return (path.equals("/api/v1/tokens") && "POST".equals(request.getMethod().name()))
                || (path.equals("/api/v1/users/login") && "POST".equals(request.getMethod().name()))
                || path.startsWith("/api/books")
                || path.matches(".*/catalogue-service/api/.*")
                || path.matches(".*/communication-service/ws-api/.*");
    }

    private Mono<Void> respondWithError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().set("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
                status.getReasonPhrase(), message);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}