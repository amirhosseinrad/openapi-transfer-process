package com.ipaam.ai.transfer.controller;

import com.ipaam.ai.transfer.model.ChatResponse;
import com.ipaam.ai.transfer.model.IntentResult;
import com.ipaam.ai.transfer.model.transfer.TransferRequest;
import com.ipaam.ai.transfer.model.whitelist.WhitelistProperties;
import com.ipaam.ai.transfer.service.BankingService;
import com.ipaam.ai.transfer.service.ChatSessionService;
import com.ipaam.ai.transfer.service.VoiceProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequestMapping("/api")
@RestController
@Slf4j
public class VoiceController {

    private final VoiceProcessingService voiceProcessingService;
    private final BankingService bankingService;
    private final WhitelistProperties whitelistProperties;
    private final ChatSessionService chatSessionService;

    public VoiceController(VoiceProcessingService voiceProcessingService, BankingService bankingService, WhitelistProperties whitelistProperties, ChatSessionService chatSessionService) {
        this.voiceProcessingService = voiceProcessingService;
        this.bankingService = bankingService;
        this.whitelistProperties = whitelistProperties;
        this.chatSessionService = chatSessionService;
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ChatResponse>> chat(@RequestPart(value = "audioFile", required = false) FilePart audioFile,
                                                   @RequestPart(value = "text", required = false) String text,
                                             @RequestParam("nationalCode") String nationalCode) {

        return resolveWhitelistEntry(nationalCode)
                .flatMap(entry -> {
                    Mono<String> transcriptMono;

                    if (text != null && !text.isBlank()) {
                        transcriptMono = Mono.just(text);
                    } else if (audioFile != null) {
                        try {
                            transcriptMono = voiceProcessingService.transcribe(audioFile);
                        } catch (IOException e) {
                            return Mono.error(new RuntimeException("❌ Failed to transcribe audio", e));
                        }
                    } else {
                        return Mono.just(ResponseEntity.badRequest().body(
                                new ChatResponse(null, "❌ No input provided. Please send either audio or text.", "FAILED", null)
                        ));
                    }

                    return transcriptMono
                            .flatMap(transcript -> {
                                chatSessionService.addUserMessage(nationalCode, transcript);

                                List<ChatSessionService.Message> fullHistory = chatSessionService.getHistory(nationalCode);

                                return voiceProcessingService.extractIntentWithHistory(
                                        fullHistory, entry.getFromAccount(), entry.getToAccount()
                                ).flatMap(intent -> {
                                    return routeIntent(intent, transcript)
                                            .doOnNext(response -> {
                                                if (response.getStatusCode().is2xxSuccessful()) {
                                                    chatSessionService.addAssistantMessage(nationalCode, Objects.requireNonNull(response.getBody()).getResultMessage());
                                                }
                                            });
                                });
                            });
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(
                        new ChatResponse(null, "❌ " + e.getMessage(), "FAILED", null)
                )));
    }

    private Mono<WhitelistProperties.WhitelistEntry> resolveWhitelistEntry(String nationalCode) {
        return whitelistProperties.getEntries().stream()
                .filter(entry -> entry.getNationalCode().equals(nationalCode))
                .findFirst()
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new IllegalArgumentException("❌ National code not whitelisted.")));
    }

    private Mono<ResponseEntity<ChatResponse>> routeIntent(IntentResult intent, String transcript) {
        if (!"1".equals(intent.getStatus())) {
            return Mono.just(ResponseEntity.badRequest().body(
                    new ChatResponse(transcript, "❌ Incomplete intent: " + intent.getMessage(), "FAILED", intent.getAction())
            ));
        }

        return switch (intent.getAction().toLowerCase()) {
            case "transfer" -> handleTransfer(intent, transcript);
            default -> Mono.just(ResponseEntity.badRequest().body(
                    new ChatResponse(transcript, "❌ Unsupported action: " + intent.getAction(), "FAILED", intent.getAction())
            ));
        };
    }

    private Mono<ResponseEntity<ChatResponse>> handleTransfer(IntentResult intent, String transcript) {
        return askUserToConfirm(intent)
                .flatMap(confirmed -> {
                    if (!confirmed) {
                        return Mono.just(ResponseEntity.badRequest().body(
                                new ChatResponse(transcript, "Transfer not confirmed by user.", "FAILED", intent.getAction())
                        ));
                    }

                    TransferRequest request = mapToTransferRequest(intent);
                    return bankingService.transferFunds(request)
                            .map(response -> {
                                if ("SUCCESS".equalsIgnoreCase(response.status())) {
                                    String msg = String.format(
                                            "✅ Transfer successful!\nTransaction ID: %s\nTracking No: %s\nDate: %s",
                                            response.transactionId(),
                                            response.trackingNumber(),
                                            response.transactionDate()
                                    );
                                    return ResponseEntity.ok(new ChatResponse(transcript, msg, "SUCCESS", intent.getAction()));
                                } else {
                                    return ResponseEntity.badRequest().body(
                                            new ChatResponse(transcript, "❌ Transfer failed: " + response.message(), "FAILED", intent.getAction())
                                    );
                                }
                            });
                });
    }

    private Mono<Boolean> askUserToConfirm(IntentResult intent) {
        // In real system, prompt user here. For now simulate "yes".
        String message = String.format("Do you confirm transfer of %s from %s to %s?",
                intent.getAmount(), intent.getFromAccount(), intent.getToAccount());

       // log.info("Asking user confirmation: {}", user);

        // Simulate confirmation (replace with actual logic)
        return Mono.just(true); // Simulated user confirmation
    }

    private TransferRequest mapToTransferRequest(IntentResult intent) {
        return new TransferRequest(
                UUID.randomUUID().toString(),             // transactionId
                null,                                     // transferBillNumber
                intent.getFromAccount(),                 // sourceAccount
                "TRANSFER",                               // documentItemType
                "001",                                    // branchCode (default/fake)
                new BigDecimal(intent.getAmount()),       // sourceAmount
                "Voice-initiated transfer",               // sourceComment
                List.of(new TransferRequest.Creditor(
                        intent.getToAccount(),            // destinationAccount
                        "TRANSFER",                       // documentItemType
                        "001",                            // branchCode
                        new BigDecimal(intent.getAmount()), // destinationAmount
                        "Voice transfer to " + intent.getToAccount() // destinationComment
                ))
        );
    }
}