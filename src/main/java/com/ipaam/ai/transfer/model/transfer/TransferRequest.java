package com.ipaam.ai.transfer.model.transfer;

import java.math.BigDecimal;
import java.util.List;

public record TransferRequest(String transactionId,
                              String transferBillNumber,
                              String sourceAccount,
                              String documentItemType,
                              String branchCode,
                              BigDecimal sourceAmount,
                              String sourceComment,
                              List<Creditor> creditors) {
    public record Creditor(
            String destinationAccount,
            String documentItemType,
            String branchCode,
            BigDecimal destinationAmount,
            String destinationComment
    ) {}
}
