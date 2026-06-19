package communications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;
    private final CatalogueContextService catalogueContextService;

//    private static final String GEMINI_URL =
//        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
//    private static final String GEMINI_URL =
//            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
//    private static final String GEMINI_URL =
//    	    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent";
    
    private static final String GEMINI_URL =
    	    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";


    public GeminiService(
            @Value("${gemini.api.key}") String apiKey,
            CatalogueContextService catalogueContextService) {
        this.apiKey = apiKey;
        this.catalogueContextService = catalogueContextService;
        this.webClient = WebClient.builder().build();
    }

    public Mono<String> generateResponse(String userMessage) {
        String systemPrompt = catalogueContextService.getCataloguePrompt();

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", systemPrompt + "\n\nUsuario: " + userMessage)
                ))
            )
        );

        return webClient.post()
            .uri(GEMINI_URL + "?key=" + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(status -> status.isError(), response ->
                response.bodyToMono(String.class)
                    .doOnNext(body -> log.error("Gemini API error - Status: {}, Body: {}",
                        response.statusCode(), body))
                    .flatMap(body -> Mono.error(new RuntimeException("Gemini error: " + body)))
            )
            .bodyToMono(Map.class)
            .map(response -> {
                try {
                    var candidates = (List<Map>) response.get("candidates");
                    var content = (Map) candidates.get(0).get("content");
                    var parts = (List<Map>) content.get("parts");
                    return (String) parts.get(0).get("text");
                } catch (Exception e) {
                    log.error("Error parsing Gemini response: {}", e.getMessage());
                    return "Lo siento, en este momento no puedo procesar tu consulta. Inténtalo de nuevo.";
                }
            })
            .onErrorResume(e -> {
                log.error("Error calling Gemini API: {}", e.getMessage());
                return Mono.just("Lo siento, en este momento el servicio no está disponible. Inténtalo más tarde.");
            });
    }
}