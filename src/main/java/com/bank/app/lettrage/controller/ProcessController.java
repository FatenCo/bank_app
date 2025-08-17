package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.ProcessDefinition;
import com.bank.app.lettrage.entity.ProcessExecution;
import com.bank.app.lettrage.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessService svc;

    public ProcessController(ProcessService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        try {
            return ResponseEntity.ok(svc.listDefinitions());
        } catch (Exception e) {
            return errorResponse("Erreur lors de la récupération des processus", e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        try {
            return svc.getDefinition(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return errorResponse("Erreur lors de la récupération du processus", e);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProcessDefinition pd) {
        try {
            return ResponseEntity.ok(svc.createDefinition(pd));
        } catch (Exception e) {
            return errorResponse("Erreur lors de la création du processus", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody ProcessDefinition pd) {
        try {
            return svc.updateDefinition(id, pd)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return errorResponse("Erreur lors de la mise à jour du processus", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            svc.deleteDefinition(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<?> runNow(@PathVariable UUID id) {
        try {
            ProcessExecution exec = svc.runNow(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "executionId", exec.getId(),
                    "processStatus", exec.getStatus().toString(),
                    "message", "Processus lancé avec succès"
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Processus non trouvé"
            ));
        } catch (Exception e) {
            return errorResponse("Échec d'exécution du processus", e);
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stop(@PathVariable UUID id) {
        try {
            svc.stopSchedule(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Planification arrêtée"
            ));
        } catch (Exception e) {
            return errorResponse("Erreur lors de l'arrêt de la planification", e);
        }
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<?> listExecutions(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(svc.listExecutions(id));
        } catch (Exception e) {
            return errorResponse("Erreur lors de la récupération de l'historique", e);
        }
    }

    private ResponseEntity<Map<String, String>> errorResponse(String message, Exception e) {
        String errorMsg = message + ": " +
                (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

        return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", errorMsg
        ));
    }
}