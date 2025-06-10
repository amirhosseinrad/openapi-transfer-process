package com.ipaam.ai.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
@Service
@RequiredArgsConstructor
public class EntityExtractionClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<String> extractEntities(String transcript, String intent) {
        Map<String, String> requestBody = Map.of(
                "transcript", transcript,
                "intent", intent
        );

        return webClientBuilder
                .baseUrl("http://localhost:8000")
                .build()
                .post()
                .uri("/extract")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }

}
