package com.bank.app.lettrage.controller;


import com.bank.app.lettrage.entity.ChatRequest;
import com.bank.app.lettrage.entity.ChatResponse;
import com.bank.app.lettrage.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatbotService.processMessage(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse(
                    "Erreur interne du serveur",
                    "error",
                    0.0
            );
            errorResponse.setSuccess(false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/chat/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "healthy");
        status.put("service", "banking-backend-chat");
        status.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/chat/test")
    public ResponseEntity<ChatResponse> testChat() {
        ChatResponse testResponse = chatbotService.processMessage("état des process");
        return ResponseEntity.ok(testResponse);
    }
}

