package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatContextService {

    private static final Logger log = LoggerFactory.getLogger(ChatContextService.class);

    // Injection optionnelle des repositories pour éviter les erreurs si certains n'existent pas
    // private final AccountRepository accountRepo;
    // private final StatementEntryRepository stmtRepo;
    // private final ReconciliationRepository recRepo;
    // private final ImportJobRepository jobRepo;

    public ChatContextService(/*AccountRepository accountRepo,
                              StatementEntryRepository stmtRepo,
                              ReconciliationRepository recRepo,
                              ImportJobRepository jobRepo*/) {
        // this.accountRepo = accountRepo;
        // this.stmtRepo = stmtRepo;
        // this.recRepo = recRepo;
        // this.jobRepo = jobRepo;
    }

    public String buildBankingContext(String userId, BankingContextData contextData) {
        try {
            // Version simplifiée pour éviter les erreurs de dépendance
            // Les statistiques sont simulées pour le moment
            long unmatchedAccounts = getUnmatchedAccountsCount();
            long unmatchedStatements = getUnmatchedStatementsCount();
            long totalReconciliations = getTotalReconciliationsCount();
            long autoMatched = getAutoMatchedCount();
            long manualMatched = getManualMatchedCount();
            long failedJobs = getFailedJobsCount();

            String specificContext = buildSpecificContext(contextData);

            return String.format("""
                === CONTEXTE LETTRAGE BANCAIRE ===
                
                ÉTAT GLOBAL:
                - Comptes non lettrés: %d
                - Relevés non lettrés: %d
                - Total réconciliations: %d
                - Automatiques réussies: %d
                - Manuelles réussies: %d
                - Jobs d'import échoués récents: %d
                
                SEUILS CONFIGURATION:
                - Tolérance lettrage: 100.00 TND
                - Mode réconciliation: Automatique + Manuel
                
                %s
                
                === AIDE DISPONIBLE ===
                Tu peux aider avec:
                - Processus de lettrage automatique/manuel
                - Résolution d'écarts et différences
                - Configuration des règles de tolérance
                - Import de fichiers bancaires
                - Analyse des échecs de réconciliation
                - Navigation dans l'application
                """,
                    unmatchedAccounts, unmatchedStatements, totalReconciliations,
                    autoMatched, manualMatched, failedJobs, specificContext
            );

        } catch (Exception e) {
            log.error("Erreur construction contexte: {}", e.getMessage(), e);
            return buildBasicContext();
        }
    }

    private String buildBasicContext() {
        return """
            === CONTEXTE LETTRAGE BANCAIRE ===
            
            Système de lettrage bancaire opérationnel.
            
            === AIDE DISPONIBLE ===
            Tu peux aider avec:
            - Processus de lettrage automatique/manuel
            - Résolution d'écarts et différences
            - Configuration des règles de tolérance
            - Import de fichiers bancaires
            - Navigation dans l'application
            """;
    }

    private String buildSpecificContext(BankingContextData contextData) {
        if (contextData == null) return "";

        StringBuilder context = new StringBuilder("CONTEXTE SPÉCIFIQUE:\n");

        if (contextData.getCurrentPage() != null) {
            context.append("- Page actuelle: ").append(contextData.getCurrentPage()).append("\n");
        }

        if (contextData.getAccountNumber() != null) {
            context.append("- Compte sélectionné: ").append(contextData.getAccountNumber());
            if (contextData.getAccountBalance() != null) {
                context.append(" (Solde: ").append(contextData.getAccountBalance()).append(" TND)");
            }
            context.append("\n");
        }

        if (contextData.getReconciliationStatus() != null) {
            context.append("- Statut réconciliation: ").append(contextData.getReconciliationStatus()).append("\n");
        }

        return context.toString();
    }

    // Méthodes simulées pour les statistiques (à remplacer par les vraies requêtes DB)
    private long getUnmatchedAccountsCount() {
        // return accountRepo != null ? safeCount(() -> accountRepo.countUnmatchedAccounts()) : 15;
        return 15; // Valeur simulée
    }

    private long getUnmatchedStatementsCount() {
        // return stmtRepo != null ? safeCount(() -> stmtRepo.countUnmatchedStatements()) : 23;
        return 23; // Valeur simulée
    }

    private long getTotalReconciliationsCount() {
        // return recRepo != null ? safeCount(() -> recRepo.count()) : 156;
        return 156; // Valeur simulée
    }

    private long getAutoMatchedCount() {
        // return recRepo != null ? safeCount(() -> recRepo.countByAutoMatched(true)) : 134;
        return 134; // Valeur simulée
    }

    private long getManualMatchedCount() {
        // return recRepo != null ? safeCount(() -> recRepo.countByManualMatched(true)) : 22;
        return 22; // Valeur simulée
    }

    private long getFailedJobsCount() {
        // Simulation de jobs échoués récents
        return 2;
    }

    // Méthode utilitaire pour gérer les erreurs de comptage
    private long safeCount(CountSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.debug("Erreur lors du comptage: {}", e.getMessage());
            return 0;
        }
    }

    @FunctionalInterface
    private interface CountSupplier {
        long get() throws Exception;
    }
}
