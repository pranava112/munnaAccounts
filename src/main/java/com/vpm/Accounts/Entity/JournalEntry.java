
// package com.vpm.Accounts.Entity;

// import jakarta.persistence.*;
// import java.util.*;
// import com.fasterxml.jackson.annotation.JsonManagedReference;

// @Entity
// public class JournalEntry {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(unique = true)
//     private String voucherNumber;

//     private String entryDate;
//     private String description;

//     @OneToMany(
//             mappedBy = "journalEntry",
//             cascade = CascadeType.ALL,
//             orphanRemoval = true,
//             fetch = FetchType.EAGER   // ⚠️ can keep, but see note below
//     )
//     @JsonManagedReference
//     private List<JournalEntryLine> lines = new ArrayList<>();


//     @ManyToOne(fetch=FetchType.LAZY)
//     private FinancialYear financialYear;

//     @ManyToOne(fetch=FetchType.LAZY)
//     private AccountingPeriod accountingPeriod;

//     // =========================
//     // ✅ GETTERS & SETTERS
//     // =========================

//     public Long getId() {
//         return id;
//     }

//     public String getVoucherNumber(){
//         return voucherNumber;
//     }

//     public void setVoucherNumber(String voucherNumber){
//         this.voucherNumber=voucherNumber;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public String getEntryDate() {
//         return entryDate;
//     }

//     public void setEntryDate(String entryDate) {
//         this.entryDate = entryDate;
//     }

//     public String getDescription() {
//         return description;
//     }

//     public void setDescription(String description) {
//         this.description = description;
//     }

//     public List<JournalEntryLine> getLines() {
//         return lines;
//     }

//     public void setLines(List<JournalEntryLine> lines) {
//         this.lines = lines;
//     }

//     public FinancialYear getFinancialYear()
//     {
//         return financialYear;
//     }

//     public void setFinancialYear(FinancialYear financialYear){
//         this.financialYear=financialYear;
//     }

//     public AccountingPeriod getAccountingPeriod(){
//         return accountingPeriod;
//     }

//     public void setAccountingPeriod(AccountingPeriod accountingPeriod){
//         this.accountingPeriod=accountingPeriod;
//     }

// }




package com.vpm.Accounts.Entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_journal_tenant_voucher",
        columnNames = {"tenant_id", "voucher_number"}))
@EntityListeners(TenantEntityListener.class)
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String voucherNumber;

    @Column(nullable = false)
    private LocalDate entryDate;

    private String description;

    @Column(name = "voucher_type", length = 32)
    private String voucherType;

    @OneToMany(
            mappedBy = "journalEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<JournalEntryLine> lines = new ArrayList<>();

    // =====================================================
    // FINANCIAL YEAR
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private FinancialYear financialYear;

    // =====================================================
    // ACCOUNTING PERIOD
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private AccountingPeriod accountingPeriod;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    // =====================================================
    // GETTERS & SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public List<JournalEntryLine> getLines() {
        return lines;
    }

    public void setLines(List<JournalEntryLine> lines) {
        this.lines = lines;
    }

    public FinancialYear getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(FinancialYear financialYear) {
        this.financialYear = financialYear;
    }

    public AccountingPeriod getAccountingPeriod() {
        return accountingPeriod;
    }

    public void setAccountingPeriod(
            AccountingPeriod accountingPeriod) {

        this.accountingPeriod = accountingPeriod;
    }

    // =====================================================
    // API-FRIENDLY IDS
    // =====================================================

    public Long getFinancialYearId() {

        return financialYear != null
                ? financialYear.getId()
                : null;
    }

    public Long getAccountingPeriodId() {

        return accountingPeriod != null
                ? accountingPeriod.getId()
                : null;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}