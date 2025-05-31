package com.ipaam.ai.transfer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ipaam.ai.transfer.model.IntentResult;
import com.ipaam.ai.transfer.service.VoiceProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {
    @Autowired
    private VoiceProcessingService voiceProcessingService;

    @PostMapping("/withdraw")
    public ResponseEntity<String> processVoice(@RequestParam MultipartFile audioFile,
                                               @RequestHeader("user-id") String userId) throws IOException {
        String transcript = String.valueOf(voiceProcessingService.transcribe(audioFile));
        IntentResult intent = voiceProcessingService.extractIntent(transcript);

        if ("withdraw".equals(intent.getAction())) {
            boolean success = voiceProcessingService.withdraw(userId, intent.getAmount(), intent.getAccountType());
            if (success) {
                return ResponseEntity.ok("Withdrawal successful.");
            } else {
                return ResponseEntity.status(400).body("Withdrawal failed.");
            }
        } else {
            return ResponseEntity.badRequest().body("Unsupported voice command.");
        }
    }
}
