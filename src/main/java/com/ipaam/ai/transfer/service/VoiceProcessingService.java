package com.ipaam.ai.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipaam.ai.transfer.client.OpenAiWebClient;
import com.ipaam.ai.transfer.client.WhisperClient;
import com.ipaam.ai.transfer.model.IntentResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class VoiceProcessingService {
    private final OpenAiWebClient openAIClient;
    private final WhisperClient whisperClient;
    public VoiceProcessingService(OpenAiWebClient openAiWebClient, OpenAiWebClient openAIClient, WhisperClient whisperClient) {
        this.openAIClient = openAIClient;
        this.whisperClient = whisperClient;
    }

    public String processIntent(String transcript) {
        return openAIClient.chat("Extract intent from: " + transcript)
                .block(); // Optional: wrap with timeout
    }

    public Mono<String> transcribe(MultipartFile audioFile) throws IOException {
        // Call Whisper API (or local Whisper service)
        // Upload audio and get transcript
        return whisperClient.transcribe(audioFile); // Abstracted
    }

    public IntentResult extractIntent(String transcript) throws JsonProcessingException {
        // Send transcript to OpenAI API for intent parsing
        String prompt = "Extract the intent from this banking command: \"" + transcript + "\". "
                + "Return JSON like {\"action\":\"withdraw\", \"amount\":2000000, \"accountType\":\"salary\"}";

        String result = String.valueOf(openAIClient.chat(prompt));
        return parseJsonToIntent(result);
    }

    public boolean withdraw(String userId, int amount, String accountType) {
        return Boolean.TRUE;
        // Call your banking withdrawal module
       // return bankingService.withdraw(userId, amount, accountType);
    }

    private IntentResult parseJsonToIntent(String json) throws JsonProcessingException {
        // Parse and map to POJO
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, IntentResult.class);
    }
}
