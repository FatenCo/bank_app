package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.AccountEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntry, UUID> {

    @Query("SELECT a FROM AccountEntry a WHERE a.total <> 0")
    List<AccountEntry> findNonZeroTotals();

    List<AccountEntry> findByAccountNumber(String accountNumber);

    @Query("""
        SELECT a FROM AccountEntry a
        WHERE a.id NOT IN (
            SELECT r.accountEntry.id FROM Reconciliation r WHERE r.matched = true
        )
        AND a.total <> 0
    """)
    List<AccountEntry> findUnmatchedAccounts();

    @Query("SELECT COUNT(a) FROM AccountEntry a WHERE a.id NOT IN " +
            "(SELECT DISTINCT r.accountEntry.id FROM Reconciliation r WHERE r.matched = true)")
    long countUnmatchedAccounts();

    // Add a missing method signature for the query below
    @Query("SELECT a FROM AccountEntry a WHERE a.id NOT IN " +
            "(SELECT DISTINCT r.accountEntry.id FROM Reconciliation r WHERE r.matched = true)")
    List<AccountEntry> findUnmatchedAccountsWithDistinct();
}
