package com.ipaam.ai.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public VoiceProcessingService(OpenAiWebClient openAiWebClient, OpenAiWebClient openAIClient, WhisperClient whisperClient) {
        this.openAIClient = openAIClient;
        this.whisperClient = whisperClient;
    }

    public Mono<String> transcribe(FilePart audioFile) throws IOException {
        return whisperClient.transcribe(audioFile); // Abstracted
    }

    public Mono<IntentResult> extractIntent(Mono<String> transcript) throws JsonProcessingException {
        return transcript.flatMap(s -> {
            String prompt = "Extract the intent from this banking command: \"" + s + "\". "
                    + "just Return JSON like {" +
                    "\"action\":\"withdraw\", " +
                    "\"amount\":2000000, " +
                    "\"fromAccount\":\"source account\" + " +
                    "\"toAccount\":\"destination account\"}";
            return openAIClient.chat(prompt); // this already returns Mono<IntentResult>
        });
    }

    public boolean withdraw(String action, String amount, String fromAccount, String toAccount ) {
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
