package com.ipaam.ai.transfer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipaam.ai.transfer.model.IntentResult;
import com.ipaam.ai.transfer.model.OpenRouterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAiWebClient {

    @Qualifier("aiWebClient")
    private final WebClient webClient;

    public OpenAiWebClient(
            WebClient.Builder webClientBuilder,
            @Value("${openrouter.api.key}") String apiKey) {

        this.webClient = webClientBuilder
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("Referer", "https://ipaam.ir") // corrected header name
                .defaultHeader("X-Title", "My OpenRouter Client") // optional custom header
                .build();
    }

    public Mono<IntentResult> chat(String prompt) {
        Map<String, Object> payload = Map.of(
                "model", "google/gemini-pro-1.5", // a known free model on OpenRouter
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 1000
        );

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(OpenRouterResponse.class)
                .flatMap(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        return Mono.error(new RuntimeException("No choices returned from OpenRouter"));
                    }
                    String content = response.getChoices().getFirst().getMessage().getContent();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String cleanJson = extractJsonFromMarkdown(content);
                        IntentResult intentResult = objectMapper.readValue(cleanJson, IntentResult.class);
                    //    log.info(intentResult.toString());
                        return Mono.just(intentResult);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse IntentResult: " + e.getMessage(), e));
                    }
                });
    }


    public Mono<IntentResult> chat(List<Map<String, String>> messages) {
        Map<String, Object> payload = Map.of(
                "model", "google/gemini-pro-1.5",
                "messages", messages,
                "max_tokens", 1000
        );

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(OpenRouterResponse.class)
                .flatMap(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        return Mono.error(new RuntimeException("No choices returned from OpenRouter"));
                    }
                    String content = response.getChoices().getFirst().getMessage().getContent();
                    try {
                        String cleanJson = extractJsonFromMarkdown(content);
                        ObjectMapper objectMapper = new ObjectMapper();
                        IntentResult intentResult = objectMapper.readValue(cleanJson, IntentResult.class);
                        return Mono.just(intentResult);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to parse IntentResult: " + e.getMessage(), e));
                    }
                });
    }


    private String extractJsonFromMarkdown(String responseBody) {
        if (responseBody.startsWith("```")) {
            int start = responseBody.indexOf("{");
            int end = responseBody.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                return responseBody.substring(start, end + 1);
            }
        }
        return responseBody; // fallback
    }
}
