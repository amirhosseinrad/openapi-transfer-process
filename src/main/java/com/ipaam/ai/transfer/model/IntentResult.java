package com.ipaam.ai.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntentResult {
    private String action;
    private int amount;
    private String accountType;
}
