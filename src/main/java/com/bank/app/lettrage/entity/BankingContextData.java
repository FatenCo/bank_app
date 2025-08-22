package com.bank.app.lettrage.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class BankingContextData {
    private String currentPage;
    private UUID selectedAccountId;
    private String accountNumber;
    private BigDecimal accountBalance;
    private String reconciliationStatus;
    private UUID lastImportJobId;

    public BankingContextData() {}

    // Getters & Setters
    public String getCurrentPage() { return currentPage; }
    public void setCurrentPage(String currentPage) { this.currentPage = currentPage; }

    public UUID getSelectedAccountId() { return selectedAccountId; }
    public void setSelectedAccountId(UUID selectedAccountId) { this.selectedAccountId = selectedAccountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getAccountBalance() { return accountBalance; }
    public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }

    public String getReconciliationStatus() { return reconciliationStatus; }
    public void setReconciliationStatus(String reconciliationStatus) { this.reconciliationStatus = reconciliationStatus; }

    public UUID getLastImportJobId() { return lastImportJobId; }
    public void setLastImportJobId(UUID lastImportJobId) { this.lastImportJobId = lastImportJobId; }
}

