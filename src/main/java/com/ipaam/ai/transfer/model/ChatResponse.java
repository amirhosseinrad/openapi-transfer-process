package com.ipaam.ai.transfer.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class ChatResponse {

    private String userMessage;   // The transcribed user input
    private String resultMessage; // Result of the processing (e.g. confirmation or error)
    private String status;        // e.g. "SUCCESS", "FAILED", "PENDING"
    private String action;        // e.g. "transfer", "withdraw"


    public ChatResponse(String userMessage, String resultMessage, String status, String action) {
        this.userMessage = userMessage;
        this.resultMessage = resultMessage;
        this.status = status;
        this.action = action;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
