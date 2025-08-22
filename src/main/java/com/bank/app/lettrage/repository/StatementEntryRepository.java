package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.StatementEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StatementEntryRepository extends JpaRepository<StatementEntry, UUID> {

    List<StatementEntry> findByAccountNumberIn(List<String> accountNumbers);

    @Query("""
        SELECT s FROM StatementEntry s
        WHERE s.id NOT IN (
            SELECT r.statementEntry.id FROM Reconciliation r WHERE r.matched = true
        )
    """)
    List<StatementEntry> findUnmatchedStatements();

    @Query("SELECT COUNT(s) FROM StatementEntry s WHERE s.id NOT IN " +
            "(SELECT DISTINCT r.statementEntry.id FROM Reconciliation r WHERE r.matched = true)")
    long countUnmatchedStatements();

    // Adding the missing method signature for the query below
    @Query("SELECT s FROM StatementEntry s WHERE s.id NOT IN " +
            "(SELECT DISTINCT r.statementEntry.id FROM Reconciliation r WHERE r.matched = true)")
    List<StatementEntry> findUnmatchedStatementsWithDistinct();  // The missing method
}
