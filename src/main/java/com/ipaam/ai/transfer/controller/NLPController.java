package com.ipaam.ai.transfer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.client.EntityExtractionClient;
import com.ipaam.ai.transfer.model.IntentResult;
import com.ipaam.ai.transfer.model.PromptRequest;
import com.ipaam.ai.transfer.model.transfer.TransferRequest;
import com.ipaam.ai.transfer.model.whitelist.WhitelistProperties;
import com.ipaam.ai.transfer.service.BankingTransferService;
import com.ipaam.ai.transfer.service.VoiceProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class NLPController {
    private final EntityExtractionClient extractionClient;
    private final VoiceProcessingService voiceProcessingService;
    private final WhitelistProperties whitelistProperties;
    private  final BankingTransferService bankingTransferService;
/*

    @PostMapping("/extract/regx")
    public Mono<String> testExtractByRegex(@RequestBody PromptRequest request) {
        return extractionClient.extractEntities(request.getPrompt(), "transfer_money");
    }
*/

    @PostMapping("/extract")
    public Mono<IntentResult> extractIntent(@RequestBody PromptRequest request, @RequestParam String nationalId) throws JsonProcessingException {

        Optional<WhitelistProperties.WhitelistEntry> matchedEntry = whitelistProperties.getEntries().stream()
                .filter(entry -> entry.getNationalCode().equals(nationalId))
                .findFirst();
        if (matchedEntry.isEmpty()) {
            return Mono.just(new IntentResult(request.getPrompt(),"کد ملی مجاز نیست."));
        }

        // Get the fromAccount and toAccount values from the matched entry
        String fromAccount = matchedEntry.get().getFromAccount();
        String toAccount = matchedEntry.get().getToAccount();


        return voiceProcessingService.extractIntent(Mono.just(request.getPrompt()), fromAccount, toAccount)
                .flatMap(intentResult -> {
                    intentResult.setMessage(request.getPrompt());
                    if ("transfer".equals(intentResult.getAction()) && "1".equals(intentResult.getStatus())) {
                        // Create the TransferRequest based on intentResult
                        TransferRequest transferRequest = buildTransferRequest(intentResult);
                        return bankingTransferService.transferFunds(transferRequest)
                                .map(response -> {
                                    // Handle the response if necessary
                                    intentResult.setResult("Transfer successful");
                                    return intentResult;
                                })
                                .onErrorResume(e -> {
                                    intentResult.setResult("Transfer failed: " + e.getMessage());
                                    return Mono.just(intentResult);
                                });
                    }
                    return Mono.just(intentResult);


                });
    }

    private TransferRequest buildTransferRequest(IntentResult intentResult) {
        // Map the values from intentResult to TransferRequest fields
        TransferRequest.Creditor creditor = new TransferRequest.Creditor(
                intentResult.getToAccount(),
                "TRANSFER", // Document type can be set here
                "branchCode", // You might need to add logic to retrieve the branch code
                new BigDecimal(intentResult.getAmount()), // Convert amount from String to BigDecimal
                "Payment for transfer"
        );

        List<TransferRequest.Creditor> creditors = new ArrayList<>();
        creditors.add(creditor);

        return new TransferRequest(
                UUID.randomUUID().toString(), // Generate a transactionId
                "billNumber", // Set the transfer bill number, you might need to define how to generate it
                intentResult.getFromAccount(),
                "TRANSFER", // Document item type
                "branchCode", // Set branch code appropriately
                new BigDecimal(intentResult.getAmount()), // Convert amount
                "Transfer initiated",
                creditors
        );
    }
}
