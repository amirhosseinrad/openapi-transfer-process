package com.ipaam.ai.transfer.controller;

import com.ipaam.ai.transfer.service.VoiceProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@RequestMapping("/api")
@RestController
public class VoiceController {

    private final VoiceProcessingService voiceProcessingService;


    public VoiceController(VoiceProcessingService voiceProcessingService) {
        this.voiceProcessingService = voiceProcessingService;
    }

/*    @Operation(
            summary = "Process a voice command for transfer",
            description = "Accepts a voice file and processes it to perform a transfer.",
            parameters = {
                    @Parameter(name = "user-id", description = "User ID", required = true, in = ParameterIn.HEADER)
            }
    )
    @PostMapping(value = "/transfer", consumes = "multipart/form-data")
    public Mono<ResponseEntity<String>> processVoice(
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
                        if ("withdrawal".equals(intent.getAction())) {
                            return Mono.fromCallable(() ->
                                            voiceProcessingService.withdraw(intent.getAction(), intent.getAmount(), intent.getFromAccount(), intent.getToAccount())
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
                    ; // blocking here to return ResponseEntity synchronously
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return Mono.just(ResponseEntity.internalServerError()
                    .body("Unexpected error: " + e.getMessage()));
        }
    }*/
    @PostMapping(value = "/transcribe",  consumes = "multipart/form-data")
    public Mono<ResponseEntity<String>> speechToText(@RequestPart("audioFile") FilePart audioFile) throws IOException {
        return voiceProcessingService.transcribe(audioFile)
                .map(transcript -> ResponseEntity.ok().body(transcript))
                .onErrorResume(e -> {
                    log.error("Unexpected error", e);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body("Unexpected error: " + e.getMessage()));
                });

    }
}