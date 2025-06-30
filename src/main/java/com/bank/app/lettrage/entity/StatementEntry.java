package com.bank.app.lettrage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "statement_entry")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class StatementEntry implements Serializable {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String contract;

    @Column(nullable = false)
    private String category;

    @Column(name = "consol_key", nullable = false)
    private String consolKey;

    @Column(nullable = false)
    private String currency;

    @Column(name = "customer_no", nullable = false)
    private String customerNo;

    @Column(nullable = false)
    private String department;

    // deux colonnes réservées non mappées :
    @Transient
    private String reserved1;

    @Column(name = "amt_fcy", precision = 19, scale = 6)
    private BigDecimal amtFcy;

    @Column(name = "amt_lcy", precision = 19, scale = 6)
    private BigDecimal amtLcy;

    @Column
    private String residence;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Transient
    private String reserved2;

    @Column(name = "lcl_bal_conv", precision = 19, scale = 6)
    private BigDecimal lclBalConv;

    /**
     * Stocké en base au format 'YYYY-MM', par exemple '2025-06'
     */
    @Column(name = "acct_date", length = 7)
    private String acctDate;

    @Column(name = "loc_contract_type")
    private String locContractType;

    @Column(name = "dept_level", precision = 19, scale = 6)
    private BigDecimal deptLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id")
    @JsonIgnore
    private ImportJob importJob;

    public StatementEntry() {
    }

    // Getters & Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConsolKey() {
        return consolKey;
    }

    public void setConsolKey(String consolKey) {
        this.consolKey = consolKey;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public BigDecimal getAmtFcy() {
        return amtFcy;
    }

    public void setAmtFcy(BigDecimal amtFcy) {
        this.amtFcy = amtFcy;
    }

    public BigDecimal getAmtLcy() {
        return amtLcy;
    }

    public void setAmtLcy(BigDecimal amtLcy) {
        this.amtLcy = amtLcy;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public BigDecimal getLclBalConv() {
        return lclBalConv;
    }

    public void setLclBalConv(BigDecimal lclBalConv) {
        this.lclBalConv = lclBalConv;
    }

    public String getAcctDate() {
        return acctDate;
    }

    public void setAcctDate(String acctDate) {
        this.acctDate = acctDate;
    }

    /**
     * Optional helper to work with YearMonth in code:
     */
    @Transient
    public YearMonth getAcctYearMonth() {
        return acctDate != null ? YearMonth.parse(acctDate) : null;
    }

    @Transient
    public void setAcctYearMonth(YearMonth ym) {
        this.acctDate = ym != null ? ym.toString() : null;
    }

    public String getLocContractType() {
        return locContractType;
    }

    public void setLocContractType(String locContractType) {
        this.locContractType = locContractType;
    }

    public BigDecimal getDeptLevel() {
        return deptLevel;
    }

    public void setDeptLevel(BigDecimal deptLevel) {
        this.deptLevel = deptLevel;
    }

    public ImportJob getImportJob() {
        return importJob;
    }

    public void setImportJob(ImportJob importJob) {
        this.importJob = importJob;
    }
}
