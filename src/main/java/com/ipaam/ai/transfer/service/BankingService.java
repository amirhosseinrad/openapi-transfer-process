package com.ipaam.ai.transfer.service;

import com.ipaam.ai.transfer.model.transfer.TransferRequest;
import com.ipaam.ai.transfer.model.transfer.TransferResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BankingService {

    @Qualifier("bankingWebClient")
    private final WebClient webClient;

    public BankingService(@Qualifier("bankingWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<TransferResponse> transferFunds(TransferRequest request) {
        return webClient.post()
                .uri("/api/corebanking/payment/v1.0/accounts/transfer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TransferResponse.class)
                .onErrorResume(e -> {
                    // Log error and return a meaningful error response
                    return Mono.error(new RuntimeException("Transfer failed: " + e.getMessage()));
                });
    }
}