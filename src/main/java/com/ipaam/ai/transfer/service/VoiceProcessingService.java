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

    public Mono<IntentResult> extractIntent(Mono<String> transcript, String fromAccount, String toAccount) throws JsonProcessingException {
        return transcript.flatMap(s -> {
            // Update the prompt to include fromAccount and toAccount
            String prompt = "Extract amount from this command : \"" + s + "\". "
                    + "Return the result as a JSON in English like and fill the amount in json {" +
                    "\"status\":"+
                    "\"message\":"+
                    "\"action\":\"transfer\", " +
                    "\"amount\":2000000, " +
                    "\"fromAccount\":\"" + fromAccount + "\", " +
                    "\"toAccount\":\"" + toAccount + "\"} and if you are not sure about any fields, fill them with 'unknown'" +
                    "if there is not 'unknown' for one of the fields put 1 in status field in json and" +
                    "if there is 'unknown' value about any filed put 0 in message field"+
                    " and dont change from and to accounts and fill them with values as i gave them to you.";

            // Call the OpenAI client to extract intent based on the updated prompt
            return openAIClient.chat(prompt); // this already returns Mono<IntentResult>
        });

    }

    public Mono<IntentResult> extractConfirmation(Mono<String> transcript) throws JsonProcessingException {
        return transcript.flatMap(s -> {
            // Update the prompt to include fromAccount and toAccount
            String prompt =  "analysis this" + transcript +" if it is means agreement make Json with one field its name is confirmed and its value is True" +
                    " and if it means disagreement set confirmed field False";
        return openAIClient.chat(prompt);
        });
    }




    private IntentResult parseJsonToIntent(String json) throws JsonProcessingException {
        // Parse and map to POJO
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, IntentResult.class);
    }
}
