package com.vpm.Accounts.Entity.GST;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.security.TenantEntityListener;
import org.hibernate.annotations.TenantId;
import java.time.LocalDate;

@Entity
@Table(name = "tax_periods")
@EntityListeners(TenantEntityListener.class)
public class TaxPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private String financialYear;
    @Column(name = "\"month\"")
    private Integer month;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean locked;
    private LocalDate lockedAt;
    private String lockedBy;
    private String gstReturnStatus;
    private String reconciliationStatus;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public LocalDate getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDate lockedAt) { this.lockedAt = lockedAt; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public String getGstReturnStatus() { return gstReturnStatus; }
    public void setGstReturnStatus(String gstReturnStatus) { this.gstReturnStatus = gstReturnStatus; }
    public String getReconciliationStatus() { return reconciliationStatus; }
    public void setReconciliationStatus(String reconciliationStatus) { this.reconciliationStatus = reconciliationStatus; }
    @JsonIgnore
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
