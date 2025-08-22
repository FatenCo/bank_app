package com.bank.app.lettrage.entity;

import java.util.List;
import java.util.Map;

public class ChatResponse {
    private String response;
    private boolean success;
    private String sessionId;
    private List<SuggestedAction> suggestedActions;
    private Map<String, Object> contextData;

    // Constructeurs, getters et setters
    public ChatResponse() {}

    public ChatResponse(String response, boolean success) {
        this.response = response;
        this.success = success;
    }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<SuggestedAction> getSuggestedActions() { return suggestedActions; }
    public void setSuggestedActions(List<SuggestedAction> suggestedActions) { this.suggestedActions = suggestedActions; }

    public Map<String, Object> getContextData() { return contextData; }
    public void setContextData(Map<String, Object> contextData) { this.contextData = contextData; }
}