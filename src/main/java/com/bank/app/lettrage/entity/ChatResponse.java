package com.bank.app.lettrage.entity;


import java.time.LocalDateTime;

public class ChatResponse {
    private String response;
    private String intent;
    private Double confidence;
    private LocalDateTime timestamp;
    private boolean success;

    public ChatResponse() {}

    public ChatResponse(String response, String intent, Double confidence) {
        this.response = response;
        this.intent = intent;
        this.confidence = confidence;
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    // Getters et Setters
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
