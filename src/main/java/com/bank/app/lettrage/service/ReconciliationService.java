package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.*;
import com.bank.app.lettrage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);
    private static final BigDecimal TOLERANCE = new BigDecimal("100.00");

    private final AccountRepository accountRepo;
    private final StatementEntryRepository stmtRepo;
    private final ReconciliationRepository recRepo;

    public ReconciliationService(AccountRepository accountRepo,
                                 StatementEntryRepository stmtRepo,
                                 ReconciliationRepository recRepo) {
        this.accountRepo = accountRepo;
        this.stmtRepo = stmtRepo;
        this.recRepo = recRepo;
    }

    public void reconcileAutomatically() {
        List<AccountEntry> accounts = accountRepo.findNonZeroTotals();
        log.info("Comptes non-zéro trouvés : {}", accounts.size());
        if (accounts.isEmpty()) return;

        Map<String, AccountEntry> accountMap = accounts.stream()
                .collect(Collectors.toMap(AccountEntry::getAccountNumber, a -> a));

        List<String> accountNumbers = new ArrayList<>(accountMap.keySet());
        List<StatementEntry> stmts = stmtRepo.findByAccountNumberIn(accountNumbers);
        log.info("Relevés chargés : {}", stmts.size());

        List<Reconciliation> toSave = new ArrayList<>();
        int autoMatchCount = 0;

        for (StatementEntry stmt : stmts) {
            AccountEntry acct = accountMap.get(stmt.getAccountNumber());

            if (acct != null) {
                BigDecimal accountAmount = acct.getTotal();
                BigDecimal statementAmount = stmt.getAmtFcy();
                BigDecimal diff = accountAmount.subtract(statementAmount).abs();

                Reconciliation rec = new Reconciliation();
                rec.setAccountEntry(acct);
                rec.setStatementEntry(stmt);
                rec.setReconciliationDate(LocalDateTime.now());

                if (accountAmount.compareTo(statementAmount) == 0) {
                    rec.setMatched(true);
                    rec.setUnmatched(false);
                    rec.setMatchingAmount(accountAmount);
                    rec.setAutoMatched(true);
                    rec.setManualMatched(false);
                    autoMatchCount++;
                    log.debug("Réconciliation automatique exacte pour compte {} et relevé {}",
                            acct.getId(), stmt.getId());
                } else {
                    rec.setMatched(false);
                    rec.setUnmatched(true);
                    rec.setMatchingAmount(BigDecimal.ZERO);
                    rec.setAutoMatched(false);
                    rec.setManualMatched(false);

                    if (diff.compareTo(TOLERANCE) <= 0) {
                        log.info("Différence dans la tolérance ({} TND) - réconciliation manuelle requise pour compte {} et relevé {}",
                                diff.setScale(2, RoundingMode.HALF_UP), acct.getId(), stmt.getId());
                    } else {
                        log.info("Différence importante ({} TND) - réconciliation manuelle avec confirmation requise pour compte {} et relevé {}",
                                diff.setScale(2, RoundingMode.HALF_UP), acct.getId(), stmt.getId());
                    }
                }

                toSave.add(rec);
            }
        }

        if (!toSave.isEmpty()) {
            recRepo.saveAll(toSave);
            log.info("{} réconciliation(s) créée(s), dont {} automatiques.", toSave.size(), autoMatchCount);
        }
    }

    public Map<String, Object> checkReconciliationDifference(UUID accountId, UUID statementId) {
        AccountEntry accountEntry = accountRepo.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("AccountEntry introuvable"));

        StatementEntry statementEntry = stmtRepo.findById(statementId)
                .orElseThrow(() -> new NoSuchElementException("StatementEntry introuvable"));

        BigDecimal accountAmount = accountEntry.getTotal();
        BigDecimal statementAmount = statementEntry.getAmtFcy();
        BigDecimal diff = accountAmount.subtract(statementAmount).abs();

        Map<String, Object> result = new HashMap<>();
        result.put("accountAmount", accountAmount);
        result.put("statementAmount", statementAmount);
        result.put("difference", diff);
        result.put("tolerance", TOLERANCE);
        result.put("isExactMatch", accountAmount.compareTo(statementAmount) == 0);
        result.put("isWithinTolerance", diff.compareTo(TOLERANCE) <= 0);
        result.put("requiresConfirmation", diff.compareTo(TOLERANCE) > 0);

        return result;
    }

    public Reconciliation manualReconcileWithTolerance(UUID accountId, UUID statementId, boolean forceReconciliation) {
        AccountEntry accountEntry = accountRepo.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("AccountEntry introuvable"));

        StatementEntry statementEntry = stmtRepo.findById(statementId)
                .orElseThrow(() -> new NoSuchElementException("StatementEntry introuvable"));

        BigDecimal accountAmount = accountEntry.getTotal();
        BigDecimal statementAmount = statementEntry.getAmtFcy();
        BigDecimal diff = accountAmount.subtract(statementAmount).abs();

        Optional<Reconciliation> existingReconciliation =
                recRepo.findByAccountEntryAndStatementEntry(accountEntry, statementEntry);

        if (accountAmount.compareTo(statementAmount) == 0) {
            log.info("Réconciliation manuelle exacte pour compte {} et relevé {}",
                    accountId, statementId);
        } else if (diff.compareTo(TOLERANCE) <= 0) {
            log.info("Réconciliation manuelle avec tolérance (diff: {} TND) pour compte {} et relevé {}",
                    diff.setScale(2, RoundingMode.HALF_UP), accountId, statementId);
        } else {
            if (!forceReconciliation) {
                throw new IllegalArgumentException(
                        String.format("Écart trop important (%.2f TND). Confirmez-vous la réconciliation ?",
                                diff.setScale(2, RoundingMode.HALF_UP).doubleValue())
                );
            }
            log.warn("Réconciliation forcée avec écart important (diff: {} TND) pour compte {} et relevé {}",
                    diff.setScale(2, RoundingMode.HALF_UP), accountId, statementId);
        }

        Reconciliation reconciliation;
        if (existingReconciliation.isPresent()) {
            reconciliation = existingReconciliation.get();
        } else {
            reconciliation = new Reconciliation();
            reconciliation.setAccountEntry(accountEntry);
            reconciliation.setStatementEntry(statementEntry);
        }

        reconciliation.setMatched(true);
        reconciliation.setUnmatched(false);
        reconciliation.setReconciliationDate(LocalDateTime.now());
        reconciliation.setMatchingAmount(accountAmount);
        reconciliation.setAutoMatched(false);
        reconciliation.setManualMatched(true);

        return recRepo.save(reconciliation);
    }

    public List<Reconciliation> listMatched() {
        return recRepo.findByMatched(true);
    }

    public List<Reconciliation> listUnmatched() {
        return recRepo.findUnmatchedReconciliations();
    }

    public List<AccountEntry> getUnmatchedAccounts() {
        return accountRepo.findUnmatchedAccounts();
    }

    public List<StatementEntry> getUnmatchedStatements() {
        return stmtRepo.findUnmatchedStatements();
    }

    public void unmatch(UUID id) {
        recRepo.findById(id).ifPresent(r -> {
            r.setMatched(false);
            r.setUnmatched(true);
            r.setAutoMatched(false);
            r.setManualMatched(false);
            recRepo.save(r);
            log.info("Réconciliation {} défaite manuellement", id);
        });
    }
}