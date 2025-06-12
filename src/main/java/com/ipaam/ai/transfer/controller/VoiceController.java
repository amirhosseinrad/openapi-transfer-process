package com.ipaam.ai.transfer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.model.IntentResult;
import com.ipaam.ai.transfer.model.transfer.TransferRequest;
import com.ipaam.ai.transfer.model.whitelist.WhitelistProperties;
import com.ipaam.ai.transfer.service.BankingService;
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
import java.util.UUID;

@RequestMapping("/api")
@RestController
@Slf4j
public class VoiceController {

    private final VoiceProcessingService voiceProcessingService;
    private final BankingService bankingService;
    private final WhitelistProperties whitelistProperties;

    public VoiceController(VoiceProcessingService voiceProcessingService, BankingService bankingService, WhitelistProperties whitelistProperties) {
        this.voiceProcessingService = voiceProcessingService;
        this.bankingService = bankingService;
        this.whitelistProperties = whitelistProperties;
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> chat(@RequestPart("audioFile") FilePart audioFile,
                                             @RequestParam("nationalCode") String nationalCode) {

        return resolveWhitelistEntry(nationalCode)
                .flatMap(entry -> {
                            try {
                                return voiceProcessingService.transcribe(audioFile)
                                        .flatMap(transcript -> {
                                            try {
                                                return voiceProcessingService.extractIntent(Mono.just(transcript), entry.getFromAccount(), entry.getToAccount());
                                            } catch (JsonProcessingException e) {
                                                return Mono.error(new RuntimeException("Failed to extract intent", e));
                                            }
                                        })
                                        .flatMap(this::routeIntent);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .onErrorResume(e -> {
                //    log.error("Error during voice chat processing", e);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body("Internal server error: " + e.getMessage()));
                });
    }

    private Mono<WhitelistProperties.WhitelistEntry> resolveWhitelistEntry(String nationalCode) {
        return whitelistProperties.getEntries().stream()
                .filter(entry -> entry.getNationalCode().equals(nationalCode))
                .findFirst()
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new IllegalArgumentException("❌ National code not whitelisted.")));
    }

    private Mono<ResponseEntity<String>> routeIntent(IntentResult intent) {
        if (!"1".equals(intent.getStatus())) {
            return Mono.just(ResponseEntity.badRequest().body("❌ Incomplete intent: " + intent.getMessage()));
        }

        return switch (intent.getAction().toLowerCase()) {
            case "transfer" -> handleTransfer(intent);
            // Add more cases for other banking actions later:
            // case "withdraw" -> handleWithdraw(intent);
            // case "deposit" -> handleDeposit(intent);
            default -> Mono.just(ResponseEntity.badRequest()
                    .body("❌ Unsupported action: " + intent.getAction()));
        };
    }

    private Mono<ResponseEntity<String>> handleTransfer(IntentResult intent) {
        return askUserToConfirm(intent)
                .flatMap(confirmed -> {
                    if (!confirmed) {
                        return Mono.just(ResponseEntity.badRequest().body("Transfer not confirmed by user."));
                    }

                    TransferRequest request = mapToTransferRequest(intent);
                    return bankingService.transferFunds(request)
                            .map(response -> {
                                if ("SUCCESS".equalsIgnoreCase(response.status())) {
                                    String msg = String.format(
                                            "Transfer successful!\nTransaction ID: %s\n Tracking No: %s\n Date: %s",
                                            response.transactionId(),
                                            response.trackingNumber(),
                                            response.transactionDate()
                                    );
                                    return ResponseEntity.ok(msg);
                                } else {
                                    return ResponseEntity.badRequest().body("❌ Transfer failed: " + response.message());
                                }
                            });
                });
    }

    private Mono<Boolean> askUserToConfirm(IntentResult intent) {
        // In real system, prompt user here. For now simulate "yes".
        String message = String.format("Do you confirm transfer of %s from %s to %s?",
                intent.getAmount(), intent.getFromAccount(), intent.getToAccount());

       // log.info("Asking user confirmation: {}", message);

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