package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.ChatRequest;
import com.bank.app.lettrage.entity.ChatResponse;
import com.bank.app.lettrage.entity.SuggestedAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BankingChatbotService {

    private static final Logger log = LoggerFactory.getLogger(BankingChatbotService.class);

    private final OllamaClientService ollamaService;
    private final ChatContextService contextService;

    public BankingChatbotService(OllamaClientService ollamaService,
                                 ChatContextService contextService) {
        this.ollamaService = ollamaService;
        this.contextService = contextService;
    }

    public ChatResponse processQuery(ChatRequest request) {
        // Validation des entrées
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            log.warn("Requête invalide reçue");
            return createErrorResponse("Veuillez saisir une question valide.");
        }

        try {
            log.info("Traitement requête chatbot: {}", request);

            // Construction du contexte bancaire avec gestion d'erreur
            String context = safelyBuildContext(request);

            // Génération du prompt spécialisé
            String prompt = buildBankingPrompt(request.getMessage().trim(), context);

            // Appel à Ollama avec gestion d'erreur
            String aiResponse = ollamaService.generateResponse(prompt);

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                return createErrorResponse("Je n'ai pas pu générer une réponse appropriée. Veuillez réessayer.");
            }

            // Construction de la réponse
            ChatResponse response = new ChatResponse(aiResponse, true);
            response.setSessionId(request.getSessionId());

            // Extraction des actions suggérées
            List<SuggestedAction> actions = extractSuggestedActions(aiResponse, request);
            response.setSuggestedActions(actions);

            // Ajout de données contextuelles
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("isOllamaHealthy", ollamaService.isHealthy());
            contextData.put("timestamp", System.currentTimeMillis());
            response.setContextData(contextData);

            log.info("Réponse chatbot générée avec succès pour utilisateur: {}", request.getUserId());
            return response;

        } catch (Exception e) {
            log.error("Erreur traitement requête chatbot pour utilisateur {}: {}",
                    request.getUserId(), e.getMessage(), e);
            return createErrorResponse("Je rencontre un problème technique. Veuillez réessayer dans quelques instants.");
        }
    }

    private String safelyBuildContext(ChatRequest request) {
        try {
            return contextService.buildBankingContext(request.getUserId(), request.getContextData());
        } catch (Exception e) {
            log.warn("Erreur construction contexte, utilisation du contexte de base: {}", e.getMessage());
            return "Contexte bancaire de base disponible.";
        }
    }

    private ChatResponse createErrorResponse(String message) {
        ChatResponse errorResponse = new ChatResponse(message, false);
        errorResponse.setSuggestedActions(getDefaultActions());
        return errorResponse;
    }

    private List<SuggestedAction> getDefaultActions() {
        List<SuggestedAction> defaultActions = new ArrayList<>();
        defaultActions.add(new SuggestedAction("NAVIGATION", "Aller au tableau de bord", "/dashboard"));
        defaultActions.add(new SuggestedAction("NAVIGATION", "Voir les réconciliations", "/reconciliation"));
        return defaultActions;
    }

    private String buildBankingPrompt(String userQuery, String bankingContext) {
        return String.format("""
            Tu es un assistant expert en réconciliation et lettrage bancaire.
            Tu travailles dans une application professionnelle de lettrage automatique.
            
            CONTEXTE SYSTÈME:
            %s
            
            QUESTION UTILISATEUR: %s
            
            INSTRUCTIONS IMPORTANTES:
            - Réponds en français de manière claire et professionnelle
            - Utilise la terminologie bancaire appropriée (lettrage, réconciliation, écart, tolérance)
            - Propose des actions concrètes quand c'est pertinent
            - Si l'utilisateur a un problème technique, guide-le étape par étape
            - Reste dans le domaine de la réconciliation bancaire
            - Limite ta réponse à 200 mots maximum
            - Si tu suggères une action, utilise les mots-clés: NAVIGATION, PROCESS, RECONCILIATION, IMPORT, CONFIGURATION
            
            RÉPONSE:
            """, bankingContext, userQuery);
    }

    private List<SuggestedAction> extractSuggestedActions(String aiResponse, ChatRequest request) {
        List<SuggestedAction> actions = new ArrayList<>();
        String response = aiResponse.toLowerCase();

        // Actions de navigation
        if (response.contains("réconciliation manuelle") || response.contains("lettrage manuel")) {
            actions.add(new SuggestedAction("NAVIGATION", "Réconciliations manuelles", "/reconciliation/manual"));
        }

        if (response.contains("import") || response.contains("fichier")) {
            actions.add(new SuggestedAction("NAVIGATION", "Import de fichiers", "/import"));
        }

        if (response.contains("configuration") || response.contains("paramètre") || response.contains("tolérance")) {
            actions.add(new SuggestedAction("NAVIGATION", "Configuration", "/configuration"));
        }

        if (response.contains("tableau de bord") || response.contains("dashboard")) {
            actions.add(new SuggestedAction("NAVIGATION", "Tableau de bord", "/dashboard"));
        }

        // Actions de processus
        if (response.contains("lancer") && response.contains("réconciliation")) {
            actions.add(new SuggestedAction("PROCESS", "Lancer réconciliation", "/api/process/reconciliation/run"));
        }

        if (response.contains("traitement") && (response.contains("compte") || response.contains("relevé"))) {
            actions.add(new SuggestedAction("PROCESS", "Traiter les fichiers", "/api/process/import/run"));
        }

        // Actions spécifiques au contexte
        if (request.getContextData() != null && request.getContextData().getSelectedAccountId() != null) {
            if (response.contains("détail") || response.contains("analyse")) {
                SuggestedAction detailAction = new SuggestedAction("NAVIGATION", "Détails du compte",
                        "/account/" + request.getContextData().getSelectedAccountId());
                detailAction.setData(request.getContextData().getSelectedAccountId());
                actions.add(detailAction);
            }
        }

        return actions;
    }
}
