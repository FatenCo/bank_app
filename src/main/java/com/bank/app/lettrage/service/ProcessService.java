package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.*;
import com.bank.app.lettrage.repository.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ProcessService {

    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);

    private final ProcessDefinitionRepository defRepo;
    private final ProcessExecutionRepository execRepo;
    private final TaskScheduler scheduler;
    private final AccountImportService accSvc;
    private final StatementImportService stmtSvc;
    private final ReconciliationService recSvc;
    private final Map<UUID, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    public ProcessService(ProcessDefinitionRepository d,
                          ProcessExecutionRepository e,
                          TaskScheduler s,
                          AccountImportService accSvc,
                          StatementImportService stmtSvc,
                          ReconciliationService recSvc) {
        this.defRepo = d;
        this.execRepo = e;
        this.scheduler = s;
        this.accSvc = accSvc;
        this.stmtSvc = stmtSvc;
        this.recSvc = recSvc;
    }

    // --- CRUD ProcessDefinition ---

    public List<ProcessDefinition> listDefinitions() {
        return defRepo.findAll();
    }

    public Optional<ProcessDefinition> getDefinition(UUID id) {
        return defRepo.findById(id);
    }

    public ProcessDefinition createDefinition(ProcessDefinition pd) {
        pd.setId(UUID.randomUUID());
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

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Nouvelle transaction
    public ProcessExecution runNow(UUID id) {
        ProcessDefinition pd = defRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Définition introuvable"));
        return execute(pd);
    }

    private ProcessExecution execute(ProcessDefinition pd) {
        ProcessExecution exec = new ProcessExecution();
        exec.setId(UUID.randomUUID());
        exec.setDefinition(pd);
        exec.setStartTime(LocalDateTime.now());
        exec.setStatus(ProcessExecStatus.RUNNING);
        execRepo.save(exec);

        try {
            log.info("Début de l'exécution pour le processus: {}", pd.getName());

            switch (pd.getType()) {
                case ACCOUNT_TREATMENT:
                    log.info("Traitement des comptes...");
                    accSvc.importByDirectory();
                    break;
                case STMT_TREATMENT:
                    log.info("Traitement des relevés...");
                    stmtSvc.importByDirectory();
                    break;
                case RECONCILIATION:
                    log.info("Démarrage de la réconciliation automatique...");
                    recSvc.reconcileAutomatically();
                    break;
                default:
                    throw new UnsupportedOperationException("Type de processus non supporté");
            }

            exec.setStatus(ProcessExecStatus.SUCCESS);
            exec.setMessage("Terminé sans erreur");
        } catch (Exception ex) {
            log.error("Erreur lors de l'exécution du processus: {}", ex.getMessage(), ex);
            exec.setStatus(ProcessExecStatus.FAILED);
            exec.setMessage("Échec: " + ex.getMessage());
        }

        exec.setEndTime(LocalDateTime.now());
        return execRepo.save(exec);
    }

    // --- Historique ---

    @Transactional(readOnly = true)
    public List<ProcessExecution> listExecutions(UUID defId) {
        return execRepo.findByDefinitionIdOrderByStartTimeDesc(defId);
    }

    // --- Planification automatique ---

    public void scheduleIfNeeded(ProcessDefinition pd) {
        if (pd.isEnabled()
                && pd.getMode() == ProcessMode.SCHEDULED
                && pd.getCronExpression() != null) {
            ScheduledFuture<?> f = scheduler.schedule(
                    () -> execute(pd),
                    new CronTrigger(pd.getCronExpression())
            );
            futures.put(pd.getId(), f);
        }
    }

    public void stopSchedule(UUID defId) {
        ScheduledFuture<?> f = futures.remove(defId);
        if (f != null) {
            f.cancel(false);
        }
    }
}