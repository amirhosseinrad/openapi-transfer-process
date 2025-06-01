package com.ipaam.ai.transfer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IntentResult {
    private String action;
    private String amount;
    private String fromAccount;
    private String toAccount;

}
