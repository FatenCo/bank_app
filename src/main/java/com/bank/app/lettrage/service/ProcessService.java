// src/main/java/com/bank/app/lettrage/service/ProcessService.java
package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.*;
import com.bank.app.lettrage.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Transactional
public class ProcessService {

    private final ProcessDefinitionRepository defRepo;
    private final ProcessExecutionRepository  execRepo;
    private final TaskScheduler               scheduler;
    private final AccountImportService        accountService;
    private final StatementImportService      stmtService;

    // pour suivre les tâches planifiées
    private final Map<UUID, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    public ProcessService(ProcessDefinitionRepository d,
                          ProcessExecutionRepository e,
                          TaskScheduler s,
                          AccountImportService accountService,
                          StatementImportService stmtService) {
        this.defRepo        = d;
        this.execRepo       = e;
        this.scheduler      = s;
        this.accountService = accountService;
        this.stmtService    = stmtService;
    }

    // --- Définitions CRUD ---
    public List<ProcessDefinition> listDefinitions() {
        return defRepo.findAll();
    }

    public Optional<ProcessDefinition> getDefinition(UUID id) {
        return defRepo.findById(id);
    }

    public ProcessDefinition createDefinition(ProcessDefinition pd) {
        ProcessDefinition saved = defRepo.save(pd);
        scheduleIfNeeded(saved);
        return saved;
    }

    public Optional<ProcessDefinition> updateDefinition(UUID id, ProcessDefinition upd) {
        return defRepo.findById(id).map(existing -> {
            stopSchedule(id);
            existing.setName(upd.getName());
            existing.setDescription(upd.getDescription());
            existing.setType(upd.getType());
            existing.setMode(upd.getMode());
            existing.setEnabled(upd.isEnabled());
            existing.setCronExpression(upd.getCronExpression());
            existing.setCronDescription(upd.getCronDescription());
            ProcessDefinition saved = defRepo.save(existing);
            scheduleIfNeeded(saved);
            return saved;
        });
    }

    public void deleteDefinition(UUID id) {
        stopSchedule(id);
        defRepo.deleteById(id);
    }

    // --- Exécution immédiate ---
    public ProcessExecution runNow(UUID defId) {
        ProcessDefinition pd = defRepo.findById(defId)
                .orElseThrow(() -> new NoSuchElementException("Définition introuvable"));
        return execute(pd);
    }

    // --- Arrêt d’un process planifié (appelé depuis le controller) ---
    public void stopSchedule(UUID defId) {
        ScheduledFuture<?> f = futures.remove(defId);
        if (f != null) {
            f.cancel(false);
        }
    }

    // --- Historique ---
    public List<ProcessExecution> listExecutions(UUID definitionId) {
        return execRepo.findByDefinitionIdOrderByStartTimeDesc(definitionId);
    }

    // --- Planification Cron auto ---
    private void scheduleIfNeeded(ProcessDefinition pd) {
        if (pd.isEnabled()
                && pd.getMode() == ProcessMode.SCHEDULED
                && pd.getCronExpression() != null
        ) {
            ScheduledFuture<?> future = scheduler.schedule(
                    () -> execute(pd),
                    new CronTrigger(pd.getCronExpression())
            );
            futures.put(pd.getId(), future);
        }
    }

    // --- cœur métier (exécution) ---
    private ProcessExecution execute(ProcessDefinition pd) {
        ProcessExecution exec = new ProcessExecution();
        exec.setDefinition(pd);
        exec.setStatus(ProcessExecStatus.RUNNING);
        exec.setStartTime(LocalDateTime.now());
        execRepo.save(exec);

        try {
            switch (pd.getType()) {
                case ACCOUNT_TREATMENT:
                    accountService.importByDirectory();
                    break;
                case STMT_TREATMENT:
                    stmtService.importByDirectory();
                    break;
                case RECONCILIATION:
                    // TODO: votre logique de rapprochement
                    break;
            }
            exec.setStatus(ProcessExecStatus.SUCCESS);
            exec.setMessage("Terminé sans erreur");
        } catch (Exception ex) {
            exec.setStatus(ProcessExecStatus.FAILED);
            exec.setMessage(ex.getMessage());
        } finally {
            exec.setEndTime(LocalDateTime.now());
            execRepo.save(exec);
        }
        return exec;
    }
}
