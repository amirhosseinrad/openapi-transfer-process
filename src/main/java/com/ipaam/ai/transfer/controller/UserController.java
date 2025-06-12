package com.ipaam.ai.transfer.controller;

import com.ipaam.ai.transfer.model.Greeting;
import com.ipaam.ai.transfer.model.whitelist.WhitelistProperties;
import com.ipaam.ai.transfer.service.CustomerInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api")
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
    public Mono<Greeting> getCustomerIdentity(@RequestParam String nationalCode, @RequestParam String birthDate) {
        Optional<WhitelistProperties.WhitelistEntry> matchedEntry = whitelistProperties.getEntries().stream()
                .filter(entry -> entry.getNationalCode().equals(nationalCode))
                .findFirst();

        if (matchedEntry.isEmpty()) {
            return Mono.just(new Greeting("کد ملی مجاز نیست."));
        }

        return customerInfoService.getCustomerInfo(nationalCode, birthDate)
                .flatMap(response -> {
                    String name = response.result().data().name();
                    String greeting = String.format(greetingMessage, name);
                    return Mono.just(new Greeting(greeting));
                })
                .onErrorResume(e -> Mono.just(new Greeting(fallbackMessage)));
    }


}
