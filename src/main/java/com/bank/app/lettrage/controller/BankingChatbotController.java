package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.ChatRequest;
import com.bank.app.lettrage.entity.ChatResponse;
import com.bank.app.lettrage.service.BankingChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class BankingChatbotController {

    private static final Logger log = LoggerFactory.getLogger(BankingChatbotController.class);

    private final BankingChatbotService chatbotService;

    public BankingChatbotController(BankingChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        try {
            log.info("Requête chatbot reçue: {}", request);

            // Validation basique
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                ChatResponse errorResponse = new ChatResponse("Veuillez saisir une question valide.", false);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ChatResponse response = chatbotService.processQuery(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur dans le contrôleur chatbot: {}", e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse(
                    "Une erreur s'est produite lors du traitement de votre demande.", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Banking Chatbot");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("chatbotService", "RUNNING");
        status.put("version", "1.0.0");
        status.put("uptime", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}