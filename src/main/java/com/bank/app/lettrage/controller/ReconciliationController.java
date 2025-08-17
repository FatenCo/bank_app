package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.Reconciliation;
import com.bank.app.lettrage.entity.AccountEntry;
import com.bank.app.lettrage.entity.StatementEntry;
import com.bank.app.lettrage.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reconciliations")
@CrossOrigin(origins = "http://localhost:4200")
public class ReconciliationController {

    @Autowired
    private ReconciliationService reconciliationService;

    @GetMapping("/matched")
    public ResponseEntity<List<Reconciliation>> getMatchedReconciliations() {
        try {
            List<Reconciliation> matched = reconciliationService.listMatched();
            return ResponseEntity.ok(matched);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/unmatched")
    public ResponseEntity<List<Reconciliation>> getUnmatchedReconciliations() {
        try {
            List<Reconciliation> unmatched = reconciliationService.listUnmatched();
            return ResponseEntity.ok(unmatched);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/unmatched/accounts")
    public ResponseEntity<List<AccountEntry>> getUnmatchedAccounts() {
        try {
            List<AccountEntry> accounts = reconciliationService.getUnmatchedAccounts();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/unmatched/statements")
    public ResponseEntity<List<StatementEntry>> getUnmatchedStatements() {
        try {
            List<StatementEntry> statements = reconciliationService.getUnmatchedStatements();
            return ResponseEntity.ok(statements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // NOUVEAU: Endpoint pour vérifier la différence avant réconciliation
    @PostMapping("/check-difference")
    public ResponseEntity<Map<String, Object>> checkDifference(@RequestBody Map<String, String> request) {
        try {
            String accountId = request.get("accountId");
            String statementId = request.get("statementId");

            Map<String, Object> result = reconciliationService.checkReconciliationDifference(
                    UUID.fromString(accountId),
                    UUID.fromString(statementId)
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // MODIFIÉ: Endpoint pour réconciliation manuelle avec confirmation
    @PostMapping("/manual-tolerance")
    public ResponseEntity<Object> manualReconcileWithTolerance(@RequestBody Map<String, Object> request) {
        try {
            String accountId = (String) request.get("accountId");
            String statementId = (String) request.get("statementId");
            Boolean forceReconciliation = (Boolean) request.getOrDefault("forceReconciliation", false);

            Reconciliation result = reconciliationService.manualReconcileWithTolerance(
                    UUID.fromString(accountId),
                    UUID.fromString(statementId),
                    forceReconciliation
            );

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Retourner une erreur structurée pour les différences importantes
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "DIFFERENCE_TOO_HIGH",
                    "message", e.getMessage(),
                    "requiresConfirmation", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "RECONCILIATION_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/unmatch/{id}")
    public ResponseEntity<Void> unmatchReconciliation(@PathVariable UUID id) {
        try {
            reconciliationService.unmatch(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}