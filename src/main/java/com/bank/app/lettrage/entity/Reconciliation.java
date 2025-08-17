package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reconciliation")
public class Reconciliation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_entry_id", nullable = false)
    private AccountEntry accountEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_entry_id", nullable = false)
    private StatementEntry statementEntry;

    @Column(name = "is_matched", nullable = false)
    private boolean matched;

    @Column(name = "unmatched", nullable = false)
    private boolean unmatched;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDateTime reconciliationDate;

    @Column(name = "matching_amount", precision = 38, scale = 2, nullable = false)
    private BigDecimal matchingAmount;

    @Column(name = "auto_matched")
    private boolean autoMatched;

    @Column(name = "manual_matched")
    private boolean manualMatched;

    public Reconciliation() {
        // Constructeur par défaut pour JPA
    }

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AccountEntry getAccountEntry() {
        return accountEntry;
    }

    public void setAccountEntry(AccountEntry accountEntry) {
        this.accountEntry = accountEntry;
    }

    public StatementEntry getStatementEntry() {
        return statementEntry;
    }

    public void setStatementEntry(StatementEntry statementEntry) {
        this.statementEntry = statementEntry;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public boolean isUnmatched() {
        return unmatched;
    }

    public void setUnmatched(boolean unmatched) {
        this.unmatched = unmatched;
    }

    public LocalDateTime getReconciliationDate() {
        return reconciliationDate;
    }

    public void setReconciliationDate(LocalDateTime reconciliationDate) {
        this.reconciliationDate = reconciliationDate;
    }

    public BigDecimal getMatchingAmount() {
        return matchingAmount;
    }

    public void setMatchingAmount(BigDecimal matchingAmount) {
        this.matchingAmount = matchingAmount;
    }

    public boolean isAutoMatched() {
        return autoMatched;
    }

    public void setAutoMatched(boolean autoMatched) {
        this.autoMatched = autoMatched;
    }

    public boolean isManualMatched() {
        return manualMatched;
    }

    public void setManualMatched(boolean manualMatched) {
        this.manualMatched = manualMatched;
    }
}