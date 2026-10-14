package com.vpm.Accounts.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;

@Entity
@Table(name = "financial_years", uniqueConstraints = @UniqueConstraint(
        name = "uk_financial_year_tenant_name",
        columnNames = {"tenant_id", "name"}))
@EntityListeners(TenantEntityListener.class)
public class FinancialYear {

    public enum Status {
        OPEN,
        CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.OPEN;

    private LocalDateTime closedAt;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @OneToMany(
            mappedBy = "financialYear",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("startDate ASC")
    @JsonIgnore
    private List<AccountingPeriod> periods = new ArrayList<>();

    // =====================================================
    // GETTERS & SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public List<AccountingPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<AccountingPeriod> periods) {
        this.periods = periods;
    }

    // =====================================================
    // BUSINESS METHODS
    // =====================================================

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    public void addPeriod(AccountingPeriod period) {

        if (period == null) {
            return;
        }

        periods.add(period);
        period.setFinancialYear(this);
    }

    public void removePeriod(AccountingPeriod period) {

        if (period == null) {
            return;
        }

        periods.remove(period);
        period.setFinancialYear(null);
    }
}