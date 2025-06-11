package com.ipaam.ai.transfer.model.openAi;

import java.util.List;

public record OpenAiResponse(List<Choice> choices) {
    public record Choice(Message message) {
        public record Message(String role, String content) {}
    }
}
