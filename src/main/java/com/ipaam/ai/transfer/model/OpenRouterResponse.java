package com.ipaam.ai.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class OpenRouterResponse {
    private List<Choice> choices;

@Data
    public static class Choice {
        private Message message;

    }
@Data
    public static class Message {
        private String role;
        private String content;

    }

}
