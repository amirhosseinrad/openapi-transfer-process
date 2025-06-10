package com.ipaam.ai.transfer.controller;

import com.ipaam.ai.transfer.model.transfer.TransferRequest;
import com.ipaam.ai.transfer.model.transfer.TransferResponse;
import com.ipaam.ai.transfer.service.BankingTransferService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final BankingTransferService transferService;

    @PostMapping
    public Mono<TransferResponse> initiateTransfer(@RequestBody TransferRequest request) {
        // You might want to validate the request here
        return transferService.transferFunds(request);
    }
}
