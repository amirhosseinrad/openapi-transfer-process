package com.ipaam.ai.transfer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.service.VoiceProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@RequestMapping("/api/voice")
@RestController
public class VoiceController {

    private final VoiceProcessingService voiceProcessingService;

    public VoiceController(VoiceProcessingService voiceProcessingService) {
        this.voiceProcessingService = voiceProcessingService;
    }

    @Operation(
            summary = "Process a voice command for withdrawal",
            description = "Accepts an MP3 file and processes it to perform a withdrawal.",
            parameters = {
                    @Parameter(name = "user-id", description = "User ID", required = true, in = ParameterIn.HEADER)
            }
    )
    @PostMapping(value = "/withdraw", consumes = "multipart/form-data")
    public ResponseEntity<String> processVoice(
            @RequestPart("audioFile") FilePart audioFile,
            @RequestHeader("user-id") Long userId) {

        try {
            return voiceProcessingService.transcribe(audioFile)
                    .flatMap(transcript -> {
                        try {
                            // extractIntent returns Mono<Intent>
                            return voiceProcessingService.extractIntent(Mono.just(transcript));
                        } catch (JsonProcessingException e) {
                            // convert checked exception to Mono.error
                            return Mono.error(e);
                        }
                    })
                    .flatMap(intent -> {
                        if ("withdraw".equals(intent.getAction())) {
                            return Mono.fromCallable(() ->
                                            voiceProcessingService.withdraw(intent.getAction(), intent.getAmount(),intent.getFromAccount(),intent.getToAccount())
                                    )
                                    .map(success -> success
                                            ? ResponseEntity.ok("Withdrawal successful.")
                                            : ResponseEntity.badRequest().body("Withdrawal failed.")
                                    );
                        } else {
                            return Mono.just(ResponseEntity.badRequest().body("Unsupported voice command."));
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Error during voice processing", e);
                        return Mono.just(ResponseEntity.internalServerError()
                                .body("Internal server error: " + e.getMessage()));
                    })
                    .block(); // blocking here to return ResponseEntity synchronously
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}