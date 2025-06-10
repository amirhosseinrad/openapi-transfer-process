package com.ipaam.ai.transfer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.client.EntityExtractionClient;
import com.ipaam.ai.transfer.model.IntentResult;
import com.ipaam.ai.transfer.model.PromptRequest;
import com.ipaam.ai.transfer.service.VoiceProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class NLPController {
    private final EntityExtractionClient extractionClient;
    private final VoiceProcessingService voiceProcessingService;

    @PostMapping("/extract/regx")
    public Mono<String> testExtractByRegex(@RequestBody PromptRequest request) {
        return extractionClient.extractEntities(request.getPrompt(), "transfer_money");
    }

    @PostMapping("/extract")
    public Mono<IntentResult> testExtractByAi(@RequestBody PromptRequest request) throws JsonProcessingException {
        return voiceProcessingService.extractIntent(Mono.just(request.getPrompt()));
    }
}
