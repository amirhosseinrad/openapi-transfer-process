package com.ipaam.ai.transfer.client;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class WhisperClient {
    private final WebClient webClient;

    public WhisperClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:5000") // Local Whisper API
                .build();
    }

    public Mono<String> transcribe(MultipartFile file) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .header("Content-Disposition", "form-data; name=file; filename=" + file.getOriginalFilename());

        return webClient.post()
                .uri("/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class);
    }
}
