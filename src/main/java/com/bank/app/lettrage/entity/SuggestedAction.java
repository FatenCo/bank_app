package com.bank.app.lettrage.entity;

public class SuggestedAction {
    private String type;
    private String label;
    private String action;
    private Object data;

    // Constructeurs, getters et setters
    public SuggestedAction() {}

    public SuggestedAction(String type, String label, String action) {
        this.type = type;
        this.label = label;
        this.action = action;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}