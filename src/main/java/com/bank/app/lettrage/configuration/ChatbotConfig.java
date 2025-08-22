package com.bank.app.lettrage.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ChatbotConfig {

    @Bean("chatbotRestTemplate")
    public RestTemplate chatbotRestTemplate(RestTemplateBuilder builder, OllamaProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Timeouts optimisés pour Ollama
        factory.setConnectTimeout(Duration.ofMillis(5000));    // 5 secondes pour la connexion
        factory.setReadTimeout(Duration.ofMillis(props.getTimeout())); // Timeout configurable

        RestTemplate restTemplate = builder
                .requestFactory(() -> factory)
                .build();

        // Ajouter un interceptor pour logger les requêtes si nécessaire
        restTemplate.getInterceptors().add((request, body, execution) -> {
            System.out.println("Appel REST vers: " + request.getURI());
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    @Bean("quickRestTemplate")
    public RestTemplate quickRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Template rapide pour les health checks
        factory.setConnectTimeout(Duration.ofMillis(3000));    // 3 secondes
        factory.setReadTimeout(Duration.ofMillis(10000));      // 10 secondes

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}