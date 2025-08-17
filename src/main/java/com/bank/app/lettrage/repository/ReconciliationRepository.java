package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.AccountEntry;
import com.bank.app.lettrage.entity.Reconciliation;
import com.bank.app.lettrage.entity.StatementEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationRepository extends JpaRepository<Reconciliation, UUID> {

    Optional<Reconciliation> findByAccountEntryAndStatementEntry(AccountEntry accountEntry, StatementEntry statementEntry);

    List<Reconciliation> findByMatched(boolean matched);

    @Query("""
        SELECT r FROM Reconciliation r
        WHERE r.matched = false
    """)
    List<Reconciliation> findUnmatchedReconciliations();

    @Query("SELECT COUNT(r) FROM Reconciliation r WHERE r.autoMatched = true")
    long countByAutoMatchedTrue();

    @Query("SELECT COUNT(r) FROM Reconciliation r WHERE r.manualMatched = true")
    long countByManualMatchedTrue();
}
