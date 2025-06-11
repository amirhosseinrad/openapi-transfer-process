package com.ipaam.ai.transfer.model.whitelist;

import lombok.Data;

@Data
public class WhitelistEntry {
    private String nationalCode;
    private String fromAccount;
    private String toAccount;
}
