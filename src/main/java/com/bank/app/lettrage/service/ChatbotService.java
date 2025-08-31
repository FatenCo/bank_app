package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.ChatResponse;
import com.bank.app.lettrage.entity.ProcessDefinition;
import com.bank.app.lettrage.entity.ProcessExecution;
import com.bank.app.lettrage.entity.ProcessType;
import com.bank.app.lettrage.entity.ProcessMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${chatbot.service.url:http://localhost:5000}")
    private String chatbotUrl;

    @Value("${chatbot.fallback.enabled:true}")
    private boolean fallbackEnabled;

    @Autowired
    private ProcessService processService;

    private final RestTemplate restTemplate;

    public ChatbotService() {
        this.restTemplate = new RestTemplate();
    }

    public ChatResponse processMessage(String message) {
        log.info("Traitement du message: {}", message);

        try {
            // 1. Tentative d'appel au microservice BERT
            String url = chatbotUrl + "/predict";
            log.info("Appel à l'URL BERT: {}", url);

            Map<String, String> request = new HashMap<>();
            request.put("message", message);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> result = response.getBody();

            log.info("Réponse BERT reçue: {}", result);

            String intent = (String) result.get("intent");
            String botResponse = (String) result.get("response");
            Double confidence = (Double) result.get("confidence");

            // 2. Exécuter actions métier
            String finalResponse = executeBusinessAction(intent, message, botResponse);

            return new ChatResponse(finalResponse, intent, confidence);

        } catch (ResourceAccessException e) {
            log.warn("Service BERT inaccessible: {}", e.getMessage());
            if (fallbackEnabled) {
                log.info("Passage en mode fallback pour le message: {}", message);
                return processFallbackMode(message);
            }
            return new ChatResponse(
                    "Service BERT non disponible. Vérifiez que le service Python tourne sur " + chatbotUrl,
                    "error",
                    0.0
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au chatbot: ", e);
            if (fallbackEnabled) {
                log.info("Passage en mode fallback à cause d'une erreur: {}", e.getMessage());
                return processFallbackMode(message);
            }
            return new ChatResponse(
                    "Erreur technique: " + e.getMessage(),
                    "error",
                    0.0
            );
        }
    }

    /**
     * Mode fallback : détection d'intent simple sans BERT
     */
    private ChatResponse processFallbackMode(String message) {
        log.info("Mode fallback activé pour: {}", message);

        String intent = detectIntentSimple(message);
        String response = executeBusinessAction(intent, message, "");

        log.info("Intent détecté en mode fallback: {} pour message: {}", intent, message);

        return new ChatResponse(response, intent, 0.8);
    }

    /**
     * Détection d'intent simple basée sur mots-clés
     */
    private String detectIntentSimple(String message) {
        String msg = message.toLowerCase().trim();

        // Mots-clés pour start_process
        if (msg.contains("start") || msg.contains("démarre") || msg.contains("lance") ||
                msg.contains("commenc") || msg.contains("démarr") || msg.contains("begin")) {
            return "start_process";
        }

        // Mots-clés pour stop_process
        if (msg.contains("stop") || msg.contains("arrête") || msg.contains("arrêt") ||
                msg.contains("interromp") || msg.contains("stopp") || msg.contains("halt")) {
            return "stop_process";
        }

        // Mots-clés pour check_process_status
        if (msg.contains("status") || msg.contains("état") || msg.contains("statut") ||
                msg.contains("check") || msg.contains("verify") || msg.contains("voir") ||
                msg.contains("monitor") || msg.contains("list")) {
            return "check_process_status";
        }

        // Mots-clés pour upload_files
        if (msg.contains("upload") || msg.contains("fichier") || msg.contains("import") ||
                msg.contains("charger") || msg.contains("téléverser") || msg.contains("csv") ||
                msg.contains("excel") || msg.contains("données")) {
            return "upload_files";
        }

        // Mots-clés pour resolve_errors
        if (msg.contains("erreur") || msg.contains("error") || msg.contains("problème") ||
                msg.contains("bug") || msg.contains("dysfonction") || msg.contains("panne") ||
                msg.contains("résoud") || msg.contains("fix") || msg.contains("debug")) {
            return "resolve_errors";
        }

        // Mots-clés pour manage_discrepancies
        if (msg.contains("écart") || msg.contains("discrepanc") || msg.contains("différence") ||
                msg.contains("divergence") || msg.contains("anomalie") || msg.contains("incohérence")) {
            return "manage_discrepancies";
        }

        // Mots-clés pour manual_reconciliation
        if (msg.contains("manuel") || msg.contains("manual") || msg.contains("main") ||
                msg.contains("manuelle")) {
            return "manual_reconciliation";
        }

        // Mots-clés pour explain_auto_reconciliation
        if (msg.contains("auto") || msg.contains("automatique") || msg.contains("automatic") ||
                msg.contains("expliqu") || msg.contains("comment") || msg.contains("fonctionn")) {
            return "explain_auto_reconciliation";
        }

        // Par défaut
        return "general_help";
    }

    private String executeBusinessAction(String intent, String message, String botResponse) {
        log.info("Exécution de l'action métier pour intent: {}", intent);

        switch (intent) {
            case "start_process":
                try {
                    ProcessDefinition reconciliationDef = createReconciliationProcess();
                    ProcessExecution execution = processService.runNow(reconciliationDef.getId());
                    log.info("Processus démarré avec succès: {}", execution.getId());
                    return "✅ Process de réconciliation démarré avec succès ! ID: " + execution.getId();
                } catch (Exception e) {
                    log.error("Erreur lors du démarrage du processus: ", e);
                    return "❌ Erreur lors du démarrage : " + e.getMessage();
                }

            case "stop_process":
                try {
                    List<ProcessDefinition> definitions = processService.listDefinitions();
                    int stoppedCount = 0;
                    for (ProcessDefinition def : definitions) {
                        if (def.isEnabled()) {
                            processService.stopSchedule(def.getId());
                            stoppedCount++;
                        }
                    }
                    log.info("Processus arrêtés: {}", stoppedCount);
                    return "⏹️ " + stoppedCount + " processus arrêtés avec succès !";
                } catch (Exception e) {
                    log.error("Erreur lors de l'arrêt des processus: ", e);
                    return "❌ Erreur lors de l'arrêt : " + e.getMessage();
                }

            case "check_process_status":
                try {
                    List<ProcessDefinition> definitions = processService.listDefinitions();
                    StringBuilder status = new StringBuilder("📊 **État des Process :**\n\n");

                    if (definitions.isEmpty()) {
                        status.append("Aucun processus défini.");
                    } else {
                        for (ProcessDefinition def : definitions) {
                            List<ProcessExecution> executions = processService.listExecutions(def.getId());

                            status.append(String.format("• **%s** - %s (%s)\n",
                                    def.getName(),
                                    def.isEnabled() ? "Actif" : "Inactif",
                                    def.getMode().toString()
                            ));

                            if (!executions.isEmpty()) {
                                ProcessExecution lastExec = executions.get(0);
                                status.append(String.format("  Dernière exécution: %s - %s\n",
                                        lastExec.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                        lastExec.getStatus().toString()
                                ));
                            }
                            status.append("\n");
                        }
                    }
                    return status.toString();
                } catch (Exception e) {
                    log.error("Erreur lors de la récupération du statut: ", e);
                    return "❌ Impossible de récupérer l'état des process: " + e.getMessage();
                }

            case "upload_files":
                return (botResponse != null ? botResponse : "Guide d'upload") +
                        "\n\n🔗 **3 méthodes disponibles :**\n" +
                        "1. Formulaire manuel\n" +
                        "2. Upload CSV/Excel\n" +
                        "3. Path dossier + process";

            case "resolve_errors":
                return "🔍 **Diagnostic erreurs :**\n" +
                        "1. Vérifiez Account Status dans les logs\n" +
                        "2. Contrôlez le format du fichier (CSV/Excel)\n" +
                        "3. Validez les colonnes requises\n" +
                        "4. Vérifiez les droits d'accès au dossier";

            case "manage_discrepancies":
                return (botResponse != null ? botResponse : "Gestion des écarts") +
                        "\n\n📋 **Actions recommandées :**\n" +
                        "• Écart ≤ 100 DT → Réconciliation manuelle\n" +
                        "• Écart > 100 DT → Validation obligatoire\n" +
                        "• Règle métier : Solde réel - Crédit réel - Débit réel - Crédit comptable + Solde comptable = 0";

            case "manual_reconciliation":
                return (botResponse != null ? botResponse : "Réconciliation manuelle") +
                        "\n\n⚠️ **Attention :** Vérifiez toujours que l'écart ne dépasse pas 100 DT avant de valider.";

            case "explain_auto_reconciliation":
                return (botResponse != null ? botResponse : "Réconciliation automatique") +
                        "\n\n🔄 **Process :** Lecture dossiers → Match montants identiques → Redirection écarts vers manuel.";

            case "general_help":
                return "🤖 **Assistant de Réconciliation Bancaire**\n\n" +
                        "Je peux vous aider avec :\n" +
                        "• Démarrer/arrêter des processus (\"start process\", \"stop process\")\n" +
                        "• Vérifier l'état (\"status\", \"état des process\")\n" +
                        "• Gérer les uploads (\"upload fichier\")\n" +
                        "• Résoudre les erreurs (\"help error\")\n" +
                        "• Traiter les écarts (\"gérer écarts\")\n\n" +
                        "Que souhaitez-vous faire ?";

            default:
                return botResponse != null ? botResponse : "Je n'ai pas compris. Tapez 'help' pour voir les commandes disponibles.";
        }
    }

    /**
     * Crée un processus de réconciliation par défaut si aucun n'existe
     */
    private ProcessDefinition createReconciliationProcess() {
        // Vérifier s'il existe déjà un processus de réconciliation
        List<ProcessDefinition> existing = processService.listDefinitions();
        for (ProcessDefinition def : existing) {
            if (def.getType() == ProcessType.RECONCILIATION) {
                log.info("Processus de réconciliation existant trouvé: {}", def.getId());
                return def;
            }
        }

        // Créer un nouveau processus de réconciliation
        ProcessDefinition reconciliationProcess = new ProcessDefinition();
        reconciliationProcess.setName("Réconciliation Automatique");
        reconciliationProcess.setDescription("Processus de réconciliation automatique démarré via chatbot");
        reconciliationProcess.setType(ProcessType.RECONCILIATION);
        reconciliationProcess.setMode(ProcessMode.MANUAL);
        reconciliationProcess.setEnabled(true);

        ProcessDefinition created = processService.createDefinition(reconciliationProcess);
        log.info("Nouveau processus de réconciliation créé: {}", created.getId());
        return created;
    }

    /**
     * Test de connectivité au service BERT
     */
    public boolean isBertServiceAvailable() {
        try {
            String healthUrl = chatbotUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Service BERT non disponible: {}", e.getMessage());
            return false;
        }
    }
}