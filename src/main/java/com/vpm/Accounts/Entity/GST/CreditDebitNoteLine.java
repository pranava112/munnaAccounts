package com.vpm.Accounts.Entity.GST;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.security.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_note_lines")
@EntityListeners(TenantEntityListener.class)
public class CreditDebitNoteLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    @JsonIgnore
    private CreditDebitNote note;

    @Column(nullable = false)
    private Long originalInvoiceLineId;

    @Column(nullable = false)
    private Long productId;

    private String description;

    private String hsnSacCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 2)
    private BigDecimal rate;

    @Column(precision = 19, scale = 2)
    private BigDecimal taxableAmount;

    @Column(precision = 7, scale = 2)
    private BigDecimal gstRate;

    @Column(precision = 7, scale = 2)
    private BigDecimal cessRate;

    @Column(precision = 19, scale = 2)
    private BigDecimal cgst;

    @Column(precision = 19, scale = 2)
    private BigDecimal sgst;

    @Column(precision = 19, scale = 2)
    private BigDecimal igst;

    @Column(precision = 19, scale = 2)
    private BigDecimal cess;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @JsonIgnore
    public CreditDebitNote getNote() { return note; }
    public void setNote(CreditDebitNote note) { this.note = note; }
    public Long getOriginalInvoiceLineId() { return originalInvoiceLineId; }
    public void setOriginalInvoiceLineId(Long originalInvoiceLineId) { this.originalInvoiceLineId = originalInvoiceLineId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHsnSacCode() { return hsnSacCode; }
    public void setHsnSacCode(String hsnSacCode) { this.hsnSacCode = hsnSacCode; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }
    public BigDecimal getCessRate() { return cessRate; }
    public void setCessRate(BigDecimal cessRate) { this.cessRate = cessRate; }
    public BigDecimal getCgst() { return cgst; }
    public void setCgst(BigDecimal cgst) { this.cgst = cgst; }
    public BigDecimal getSgst() { return sgst; }
    public void setSgst(BigDecimal sgst) { this.sgst = sgst; }
    public BigDecimal getIgst() { return igst; }
    public void setIgst(BigDecimal igst) { this.igst = igst; }
    public BigDecimal getCess() { return cess; }
    public void setCess(BigDecimal cess) { this.cess = cess; }
    @JsonIgnore
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
