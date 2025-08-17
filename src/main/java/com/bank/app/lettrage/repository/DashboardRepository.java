package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.Reconciliation;
import com.bank.app.lettrage.entity.StatementEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<StatementEntry, Long> {

    // Correction: Retourner une liste d'objets au lieu de Map
    @Query("SELECT s.acctDate AS period, COUNT(s.id) AS volume FROM StatementEntry s GROUP BY s.acctDate")
    List<Object[]> getTransactionVolumeByPeriod();

    // Correction: Retourner une liste d'objets au lieu de Map
    @Query("SELECT r.matched AS matched, COUNT(r.id) AS count FROM Reconciliation r GROUP BY r.matched")
    List<Object[]> getReconciliationStatus();

    // Correction: Retourner une liste d'objets au lieu de Map
    @Query("SELECT r.matched AS matched, AVG(CAST(r.matchingAmount AS double)) AS avgMatchingAmount FROM Reconciliation r GROUP BY r.matched")
    List<Object[]> getReconciliationPerformance();

    // Correction: Utiliser UUID au lieu de Long pour les IDs
    @Query("SELECT CAST(s.id AS string) FROM StatementEntry s WHERE s.amtFcy IS NULL")
    List<String> getPendingTransactions();

    // Query correcte pour les réconciliations non appariées
    @Query("SELECT r FROM Reconciliation r WHERE r.matched = false")
    List<Reconciliation> getUnmatchedReconciliations();
}