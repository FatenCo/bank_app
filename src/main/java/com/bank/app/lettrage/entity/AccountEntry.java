package com.bank.app.lettrage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_entry")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AccountEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "date_operation", nullable = false)
    private String dateOperation;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String entity;

    @Column
    private String remarks;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column
    private BigDecimal total;

    /**
     * Relation vers le job d'import ; on l'ignore côté JSON pour éviter
     * les problèmes de proxy ou de boucle.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id")
    @JsonIgnore
    private ImportJob importJob;

    // === Constructeurs ===

    public AccountEntry() {}

    // === Getters & Setters ===

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(String dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public ImportJob getImportJob() {
        return importJob;
    }

    public void setImportJob(ImportJob importJob) {
        this.importJob = importJob;
    }
}
