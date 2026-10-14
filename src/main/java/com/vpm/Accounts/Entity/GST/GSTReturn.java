package com.vpm.Accounts.Entity.GST;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import com.vpm.Accounts.security.TenantEntityListener;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "gst_returns")
@EntityListeners(TenantEntityListener.class)
public class GSTReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gstin;

    private String financialYear;

    @Column(name = "\"month\"")
    private Integer month;

    private BigDecimal outwardTaxable;

    private BigDecimal outwardCGST;

    private BigDecimal outwardSGST;

    private BigDecimal outwardIGST;

    private BigDecimal outwardCess;

    private BigDecimal inputCGST;

    private BigDecimal inputSGST;

    private BigDecimal inputIGST;

    private BigDecimal inputCess;

    private BigDecimal itcReversal;

    private BigDecimal reverseCharge;

    private BigDecimal taxPayable;

    private BigDecimal taxPaid;

    private String status;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    public Long getId() { return id; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public BigDecimal getOutwardTaxable() { return outwardTaxable; }
    public void setOutwardTaxable(BigDecimal outwardTaxable) { this.outwardTaxable = outwardTaxable; }
    public BigDecimal getOutwardCGST() { return outwardCGST; }
    public void setOutwardCGST(BigDecimal outwardCGST) { this.outwardCGST = outwardCGST; }
    public BigDecimal getOutwardSGST() { return outwardSGST; }
    public void setOutwardSGST(BigDecimal outwardSGST) { this.outwardSGST = outwardSGST; }
    public BigDecimal getOutwardIGST() { return outwardIGST; }
    public void setOutwardIGST(BigDecimal outwardIGST) { this.outwardIGST = outwardIGST; }
    public BigDecimal getOutwardCess() { return outwardCess; }
    public void setOutwardCess(BigDecimal outwardCess) { this.outwardCess = outwardCess; }
    public BigDecimal getInputCGST() { return inputCGST; }
    public void setInputCGST(BigDecimal inputCGST) { this.inputCGST = inputCGST; }
    public BigDecimal getInputSGST() { return inputSGST; }
    public void setInputSGST(BigDecimal inputSGST) { this.inputSGST = inputSGST; }
    public BigDecimal getInputIGST() { return inputIGST; }
    public void setInputIGST(BigDecimal inputIGST) { this.inputIGST = inputIGST; }
    public BigDecimal getInputCess() { return inputCess; }
    public void setInputCess(BigDecimal inputCess) { this.inputCess = inputCess; }
    public BigDecimal getItcReversal() { return itcReversal; }
    public void setItcReversal(BigDecimal itcReversal) { this.itcReversal = itcReversal; }
    public BigDecimal getReverseCharge() { return reverseCharge; }
    public void setReverseCharge(BigDecimal reverseCharge) { this.reverseCharge = reverseCharge; }
    public BigDecimal getTaxPayable() { return taxPayable; }
    public void setTaxPayable(BigDecimal taxPayable) { this.taxPayable = taxPayable; }
    public BigDecimal getTaxPaid() { return taxPaid; }
    public void setTaxPaid(BigDecimal taxPaid) { this.taxPaid = taxPaid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @JsonIgnore
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
