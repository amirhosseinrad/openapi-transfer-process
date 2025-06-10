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

    // Method to list available models (run this to find your valid models)
    public Mono<String> listModels() {
        return webClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(modelsJson -> System.out.println("Available models: " + modelsJson));
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
                    String content = response.getChoices().get(0).getMessage().getContent();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String cleanJson = extractJsonFromMarkdown(content);
                        IntentResult intentResult = objectMapper.readValue(cleanJson, IntentResult.class);
                        log.info(intentResult.toString());
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

    /*private boolean isEmptyResponseException(Throwable ex) {
        if (ex instanceof RuntimeException && ex.getCause() instanceof MismatchedInputException) {
            return ex.getCause().getMessage() != null &&
                    ex.getCause().getMessage().contains("No content to map due to end-of-input");
        }
        return false;
    }

    public Mono<IntentResult> analyzeText(String text) {
        return webClient.post()
                .uri("/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AnalyzeRequest(text))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("4xx Error: " + errorBody))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("5xx Error: " + errorBody))))
                .bodyToMono(String.class)
                .map(response -> {
                    String cleanJson = extractJsonFromMarkdown(response);
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        IntentResult intentResult = objectMapper.readValue(cleanJson, IntentResult.class);
                        log.info("IntentResult: {}", intentResult);
                        return intentResult;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error parsing IntentResult", e);
                    }
                });

    }*/
}
