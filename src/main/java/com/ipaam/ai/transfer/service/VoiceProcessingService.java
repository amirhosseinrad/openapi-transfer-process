package com.ipaam.ai.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.client.OpenAiWebClient;
import com.ipaam.ai.transfer.client.WhisperClient;
import com.ipaam.ai.transfer.model.IntentResult;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class VoiceProcessingService {
    private final OpenAiWebClient openAIClient;
    private final WhisperClient whisperClient;

    public VoiceProcessingService(OpenAiWebClient openAIClient, WhisperClient whisperClient) {
        this.openAIClient = openAIClient;
        this.whisperClient = whisperClient;
    }

    public Mono<String> transcribe(FilePart audioFile) throws IOException {
        return whisperClient.transcribe(audioFile); // Abstracted
    }
    public Mono<IntentResult> extractIntent(Mono<String> transcript, String fromAccount, String toAccount) throws JsonProcessingException {
        return transcript.flatMap(s -> {
            String prompt = "Extract amount from this command: \"" + s + "\". "
                    + "Return the result as a JSON in English like: " +
                    "{\"status\":1, " +
                    "\"message\":\"ok\", " +
                    "\"action\":\"transfer\", " +
                    "\"amount\":2000000, " +
                    "\"fromAccount\":\"" + fromAccount + "\", " +
                    "\"toAccount\":\"" + toAccount + "\"}. " +
                    "If any field is uncertain, fill it with 'unknown'. " +
                    "If there is any 'unknown', set status to 0. " +
                    "Keep fromAccount and toAccount unchanged.";

            return openAIClient.chat(prompt); // Already returns Mono<IntentResult>
        });
    }
}


