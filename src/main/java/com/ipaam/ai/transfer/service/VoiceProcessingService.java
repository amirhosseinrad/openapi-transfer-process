package com.ipaam.ai.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.client.OpenAiWebClient;
import com.ipaam.ai.transfer.client.WhisperClient;
import com.ipaam.ai.transfer.model.IntentResult;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            String prompt = """
                        You are an AI assistant helping with banking services.

                        Analyze the following transcript:
                        \"%s\"

                        Your task:
                        - Detect the user's **intent** (e.g., transfer, withdraw)
                        - Extract the **amount**, **fromAccount**, and **toAccount**
                        - Check if the user **confirms** or **requests** a transfer

                        Return a JSON in this exact format:
                        {
                          "status": 1, // Set to 1 only if user clearly confirms the action, otherwise 0
                          "message": "<original transcript or summary>",
                          "action": "transfer",
                          "amount": 2000000,
                          "fromAccount": "%s",
                          "toAccount": "%s"
                        }

                        Rules:
                        - If any field (like amount, intent, or confirmation) is unclear or missing, use 'unknown'
                        - If any value is 'unknown', set status to 0
                        - Do not change the fromAccount or toAccount values provided
                        - The 'message' field must include the full transcript or a user-friendly summary of it
                    """.formatted(s, fromAccount, toAccount);
            return openAIClient.chat(prompt); // returns Mono<IntentResult>
        });
    }


    public Mono<IntentResult> extractIntentWithHistory(List<ChatSessionService.Message> history,
                                                       String fromAccount, String toAccount) {
        String systemPrompt = """
            You are an AI assistant helping with banking services.

            Your task:
            - Detect the user's **intent** (e.g., transfer, withdraw)
            - Extract the **amount**, **fromAccount**, and **toAccount**
            - Check if the user **confirms** or **requests** a transfer

            Return a JSON in this exact format:
            {
              "status": 1,
              "message": "<original transcript or summary>",
              "action": "transfer",
              "amount": 2000000,
              "fromAccount": "%s",
              "toAccount": "%s"
            }

            Rules:
            - If any field (like amount, intent, or confirmation) is unclear or missing, use 'unknown'
            - If any value is 'unknown', set status to 0
            - Do not change the fromAccount or toAccount values provided
            - The 'message' field must include the full transcript or a user-friendly summary of it
            """.formatted(fromAccount, toAccount);

        // Build OpenRouter-style user list
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        for (ChatSessionService.Message m : history) {
            messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }

        return openAIClient.chat(messages);
    }


}


