// src/main/java/com/bank/app/lettrage/controller/ProcessController.java
package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.*;
import com.bank.app.lettrage.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final ProcessService svc;

    public ProcessController(ProcessService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<ProcessDefinition> list() {
        return svc.listDefinitions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessDefinition> get(@PathVariable UUID id) {
        return svc.getDefinition(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProcessDefinition create(@RequestBody ProcessDefinition pd) {
        return svc.createDefinition(pd);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessDefinition> update(
            @PathVariable UUID id,
            @RequestBody ProcessDefinition pd
    ) {
        return svc.updateDefinition(id, pd)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        svc.deleteDefinition(id);
        return ResponseEntity.noContent().build();
    }

    /** Exécution immédiate **/
    @PostMapping("/{id}/run")
    public ProcessExecution runNow(@PathVariable UUID id) {
        return svc.runNow(id);
    }

    /** Arrêt d’une planification Cron **/
    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stop(@PathVariable UUID id) {
        svc.stopSchedule(id);
        return ResponseEntity.noContent().build();
    }

    /** Récupérer les exécutions passées **/
    @GetMapping("/{id}/executions")
    public List<ProcessExecution> listExecutions(@PathVariable UUID id) {
        return svc.listExecutions(id);
    }
}
