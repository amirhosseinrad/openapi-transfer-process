package com.ipaam.ai.transfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("aiWebClient")
    public WebClient aiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    @Bean("bankingWebClient")
    public WebClient bankingWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://192.168.179.20:8290")
                .defaultHeader("Content-Type", "application/json")
                // Add any authentication headers if needed
                // .defaultHeader("Authorization", "Bearer ...")
                .build();
    }
}
