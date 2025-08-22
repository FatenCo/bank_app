package com.bank.app.lettrage.service;

import com.bank.app.lettrage.configuration.OllamaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OllamaClientService {

    private static final Logger log = LoggerFactory.getLogger(OllamaClientService.class);

    // Messages d'erreur
    private static final String DEFAULT_ERROR_MESSAGE = "Le service IA est temporairement indisponible. Veuillez réessayer dans quelques instants.";
    private static final String TIMEOUT_ERROR_MESSAGE = "La requête prend trop de temps. Veuillez reformuler votre question de manière plus concise.";

    private final RestTemplate restTemplate;
    private final RestTemplate quickRestTemplate;
    private final OllamaProperties ollamaProps;
    private final ExecutorService executorService;

    public OllamaClientService(
            @Qualifier("chatbotRestTemplate") RestTemplate chatbotRestTemplate,
            @Qualifier("quickRestTemplate") RestTemplate quickRestTemplate,
            OllamaProperties ollamaProps) {
        this.restTemplate = chatbotRestTemplate;
        this.quickRestTemplate = quickRestTemplate;
        this.ollamaProps = ollamaProps;
        this.executorService = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "ollama-client-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
    }

    public String generateResponse(String prompt) {
        log.info("Début génération réponse Ollama pour prompt: {} caractères", prompt.length());

        // Vérification rapide de la santé avant l'appel principal
        if (!isHealthy()) {
            log.warn("Ollama n'est pas en bonne santé, utilisation de la réponse de fallback");
            return getFallbackResponse(prompt);
        }

        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                return callOllamaAPI(prompt);
            }, executorService);

            // Timeout légèrement plus court que la configuration
            String result = future.get(ollamaProps.getTimeout() - 2000, TimeUnit.MILLISECONDS);
            log.info("Réponse Ollama générée avec succès: {} caractères", result.length());
            return result;

        } catch (TimeoutException e) {
            log.warn("Timeout lors de l'appel à Ollama après {} secondes", ollamaProps.getTimeout() / 1000);
            return TIMEOUT_ERROR_MESSAGE;
        } catch (Exception e) {
            log.error("Erreur lors de la génération de réponse: {}", e.getMessage(), e);
            return getFallbackResponse(prompt);
        }
    }

    private String callOllamaAPI(String prompt) {
        try {
            String optimizedPrompt = optimizePrompt(prompt);

            Map<String, Object> request = Map.of(
                    "model", ollamaProps.getModel(),
                    "prompt", optimizedPrompt,
                    "stream", false,
                    "options", Map.of(
                            "temperature", ollamaProps.getTemperature(),
                            "num_predict", Math.min(ollamaProps.getMaxTokens(), 150), // Limiter pour la vitesse
                            "top_p", 0.9,
                            "top_k", 30,
                            "repeat_penalty", 1.1,
                            "stop", new String[]{"Human:", "Assistant:", "\n\nQuestion:", "\n\nRéponse:"}
                    )
            );

            log.debug("Envoi requête Ollama: modèle={}, tokens_max={}",
                    ollamaProps.getModel(), Math.min(ollamaProps.getMaxTokens(), 150));

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ollamaProps.getUrl() + "/api/generate",
                    request,
                    Map.class
            );

            return processResponse(response);

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException ||
                    e.getMessage().contains("timeout") ||
                    e.getMessage().contains("Request timed out")) {
                log.warn("Timeout détecté dans l'appel REST à Ollama: {}", e.getMessage());
                throw new RuntimeException("TIMEOUT", e);
            }
            log.error("Erreur de connexion Ollama: {}", e.getMessage());
            throw e;
        }
    }

    private String optimizePrompt(String prompt) {
        // Optimiser le prompt pour une réponse plus rapide et ciblée
        StringBuilder optimized = new StringBuilder();

        optimized.append("Tu es un assistant bancaire expert en réconciliation. ");
        optimized.append("Réponds de manière concise et directe en français. ");
        optimized.append("Maximum 3-4 phrases.\n\n");
        optimized.append("Question: ").append(prompt.trim());

        String result = optimized.toString();

        // Limiter la taille du prompt
        if (result.length() > 1000) {
            result = result.substring(0, 997) + "...";
            log.debug("Prompt optimisé et tronqué à 1000 caractères");
        }

        return result;
    }

    private String processResponse(ResponseEntity<Map> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object responseObj = response.getBody().get("response");
            if (responseObj != null) {
                String result = responseObj.toString().trim();

                if (result.isEmpty()) {
                    log.warn("Réponse vide reçue d'Ollama");
                    return "Je n'ai pas pu générer une réponse appropriée. Veuillez reformuler votre question.";
                }

                // Nettoyer la réponse
                result = cleanResponse(result);

                // Limiter la taille de la réponse
                if (result.length() > 800) {
                    result = result.substring(0, 797) + "...";
                    log.debug("Réponse tronquée à 800 caractères");
                }

                log.debug("Réponse Ollama traitée ({} caractères)", result.length());
                return result;
            }
        }

        log.error("Réponse invalide d'Ollama: status={}, body={}",
                response.getStatusCode(), response.getBody());
        return DEFAULT_ERROR_MESSAGE;
    }

    private String cleanResponse(String response) {
        if (response == null) return "";

        return response
                .replaceAll("(?m)^(Human:|Assistant:|Question:|Réponse:)", "")
                .replaceAll("\\n{3,}", "\n\n") // Réduire les sauts de ligne multiples
                .trim();
    }

    public boolean isHealthy() {
        try {
            CompletableFuture<Boolean> healthCheck = CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("Vérification santé Ollama: {}", ollamaProps.getUrl());
                    ResponseEntity<String> response = quickRestTemplate.getForEntity(
                            ollamaProps.getUrl() + "/api/tags",
                            String.class
                    );
                    boolean healthy = response.getStatusCode().is2xxSuccessful();
                    log.debug("Ollama health check result: {}", healthy);
                    return healthy;
                } catch (Exception e) {
                    log.debug("Health check exception: {}", e.getMessage());
                    return false;
                }
            }, executorService);

            return healthCheck.get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.warn("Health check Ollama échoué: {}", e.getMessage());
            return false;
        }
    }

    private String getFallbackResponse(String prompt) {
        String lowerPrompt = prompt.toLowerCase();

        // Réponses de fallback spécialisées selon le contexte
        if (lowerPrompt.contains("import") && lowerPrompt.contains("fichier")) {
            return "Pour l'import de fichiers bancaires :\n" +
                    "• Vérifiez le format (CSV, MT940, XML)\n" +
                    "• Contrôlez le mapping des colonnes\n" +
                    "• Vérifiez les règles de validation\n" +
                    "• Consultez les logs en cas d'erreur";
        }

        if (lowerPrompt.contains("réconciliation") || lowerPrompt.contains("lettrage")) {
            return "Pour la réconciliation bancaire :\n" +
                    "• Utilisez d'abord le lettrage automatique\n" +
                    "• Traitez les écarts manuellement\n" +
                    "• Configurez les tolérances appropriées\n" +
                    "• Vérifiez les comptes non lettrés";
        }

        if (lowerPrompt.contains("échec") || lowerPrompt.contains("erreur")) {
            return "En cas d'échec :\n" +
                    "• Consultez les logs détaillés\n" +
                    "• Vérifiez la configuration\n" +
                    "• Contrôlez les données d'entrée\n" +
                    "• Contactez le support si nécessaire";
        }

        if (lowerPrompt.contains("tolérance")) {
            return "Configuration des tolérances :\n" +
                    "• Définissez les écarts acceptables\n" +
                    "• Configurez par type d'opération\n" +
                    "• Testez avec des données réelles\n" +
                    "• Ajustez selon les résultats";
        }

        return "Le service IA est temporairement indisponible. " +
                "Pour une assistance immédiate :\n" +
                "• Consultez la documentation\n" +
                "• Vérifiez les paramètres de configuration\n" +
                "• Contactez le support technique";
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            log.info("Arrêt du service Ollama...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}