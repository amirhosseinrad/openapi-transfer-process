package com.ipaam.ai.transfer.model.whitelist;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "whitelist")
public class WhitelistProperties {

    private List<WhitelistEntry> entries;

    public List<WhitelistEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<WhitelistEntry> entries) {
        this.entries = entries;
    }

    public static class WhitelistEntry {
        private String nationalCode;
        private String fromAccount;
        private String toAccount;

        // Getters and setters
        public String getNationalCode() {
            return nationalCode;
        }

        public void setNationalCode(String nationalCode) {
            this.nationalCode = nationalCode;
        }

        public String getFromAccount() {
            return fromAccount;
        }

        public void setFromAccount(String fromAccount) {
            this.fromAccount = fromAccount;
        }

        public String getToAccount() {
            return toAccount;
        }

        public void setToAccount(String toAccount) {
            this.toAccount = toAccount;
        }
    }
}