package com.ipaam.ai.transfer.service;

import com.ipaam.ai.transfer.model.userIdentity.CustomerInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CustomerInfoService {

    private final WebClient webClient;

    public CustomerInfoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://192.168.179.20:8290").build();
    }
    public Mono<CustomerInfoResponse> getCustomerInfo(String nationalCode, String birthDate) {
        return webClient.get()
                .uri("/api/corebanking/customers/v1.0/identity/{nationalCode}/{birthDate}",
                        nationalCode, birthDate)
                .retrieve()

                .bodyToMono(CustomerInfoResponse.class);
    }
}
