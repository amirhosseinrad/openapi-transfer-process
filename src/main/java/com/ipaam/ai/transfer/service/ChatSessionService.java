package com.ipaam.ai.transfer.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatSessionService {

    // Key = session ID (e.g. national code), Value = chat history
    private final ConcurrentHashMap<String, List<Message>> sessionHistory = new ConcurrentHashMap<>();

    /**
     * Append a new user user to the session.
     */
    public void addUserMessage(String sessionId, String content) {
        sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(new Message("user", content));
    }

    /**
     * Append an assistant (AI) response to the session.
     */
    public void addAssistantMessage(String sessionId, String content) {
        sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(new Message("assistant", content));
    }

    /**
     * Retrieve the full user history for this session.
     */
    public List<Message> getHistory(String sessionId) {
        return sessionHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    /**
     * Clear the history for this session.
     */
    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
    }

    public static class Message {
        private final String role; // "user" or "assistant"
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
