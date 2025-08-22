package com.bank.app.lettrage.entity;

import java.util.UUID;

public class ChatRequest {
    private String message;
    private String userId; // Changé de UUID vers String pour plus de flexibilité
    private String sessionId;
    private BankingContextData contextData;

    public ChatRequest() {}

    public ChatRequest(String message, String userId, String sessionId) {
        this.message = message;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    // Getters & Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public BankingContextData getContextData() { return contextData; }
    public void setContextData(BankingContextData contextData) { this.contextData = contextData; }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", messageLength=" + (message != null ? message.length() : 0) +
                '}';
    }
}