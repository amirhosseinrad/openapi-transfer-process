package com.ipaam.ai.transfer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IntentResult {
    private String message;
    private String action;
    private String amount;
    private String fromAccount;
    private String toAccount;
    private String Status;
    private String result;


    public IntentResult(String message, String result) {
        this.message = message;
        this.result = result;
    }
}
