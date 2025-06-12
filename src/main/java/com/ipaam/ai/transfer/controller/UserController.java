package com.ipaam.ai.transfer.controller;

import com.ipaam.ai.transfer.model.Greeting;
import com.ipaam.ai.transfer.model.whitelist.WhitelistProperties;
import com.ipaam.ai.transfer.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {

    private final CustomerInfoService customerInfoService;
    @Value("${greeting.message.persian}")
    private String greetingMessage;

    @Value("${greeting.message.fallback}")
    private String fallbackMessage;

    private final WhitelistProperties whitelistProperties;

    public UserController(CustomerInfoService customerInfoService, WhitelistProperties whitelistProperties) {
        this.customerInfoService = customerInfoService;
        this.whitelistProperties = whitelistProperties;
    }

    @PostMapping("/greeting")
    public Mono<ResponseEntity<Greeting>> getCustomerIdentity(@RequestParam String nationalCode,
                                                              @RequestParam String birthDate) {
        try {
            Optional<WhitelistProperties.WhitelistEntry> matchedEntry = whitelistProperties.getEntries().stream()
                    .filter(entry -> entry.getNationalCode().equals(nationalCode))
                    .findFirst();

            if (matchedEntry.isEmpty()) {
                throw new RuntimeException("❌ National code not whitelisted.");
            }

            return customerInfoService.getCustomerInfo(nationalCode, birthDate)
                    .flatMap(response -> {
                        String name = response.result().data().name();
                        String greeting = String.format(greetingMessage, name);
                        return Mono.just(ResponseEntity.ok(new Greeting(greeting)));
                    })
                    .onErrorResume(e -> {
                        //log.error("Error getting greeting: ", e);
                        return Mono.just(ResponseEntity.internalServerError().body(new Greeting(fallbackMessage)));
                    });

        } catch (Exception e) {
            //log.error("Unexpected error in greeting controller: ", e);
            return Mono.just(ResponseEntity.internalServerError().body(new Greeting(e.getMessage())));
        }
    }



}
