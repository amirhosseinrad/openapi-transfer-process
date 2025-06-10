package com.ipaam.ai.transfer.model.transfer;

import java.time.LocalDateTime;

public record TransferResponse(String status,
                               String message,
                               String transactionId,
                               String trackingNumber,
                               LocalDateTime transactionDate) {
}
