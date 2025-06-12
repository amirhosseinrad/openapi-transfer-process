package com.ipaam.ai.transfer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WhisperClient {
    private final WebClient.Builder webClientBuilder;

    public WhisperClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<String> transcribe(FilePart filePart) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", filePart)
                .header("Content-Disposition", "form-data; name=file; filename=" + filePart.filename())
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return webClientBuilder.baseUrl("http://localhost:5000")
                .build()
                .post()
                .uri("/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class);
    }
}
