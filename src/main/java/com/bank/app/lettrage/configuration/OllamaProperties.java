package com.bank.app.lettrage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaProperties {

    private String url = "http://localhost:11434";
    private String model = "llama3.2:3b";
    private int timeout = 30000;
    private double temperature = 0.3;
    private int maxTokens = 500;

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public String toString() {
        return "OllamaProperties{" +
                "url='" + url + '\'' +
                ", model='" + model + '\'' +
                ", timeout=" + timeout +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                '}';
    }
}