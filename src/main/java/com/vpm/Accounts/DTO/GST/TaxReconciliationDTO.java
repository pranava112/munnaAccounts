package com.vpm.Accounts.DTO.GST;

import java.math.BigDecimal;

public class TaxReconciliationDTO {
    private Long periodId;
    private String status;
    private BigDecimal invoiceTaxable = BigDecimal.ZERO;
    private BigDecimal returnTaxable = BigDecimal.ZERO;
    private BigDecimal taxableDifference = BigDecimal.ZERO;
    private BigDecimal invoiceTax = BigDecimal.ZERO;
    private BigDecimal returnTax = BigDecimal.ZERO;
    private BigDecimal taxDifference = BigDecimal.ZERO;

    public Long getPeriodId() { return periodId; }
    public void setPeriodId(Long periodId) { this.periodId = periodId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getInvoiceTaxable() { return invoiceTaxable; }
    public void setInvoiceTaxable(BigDecimal invoiceTaxable) { this.invoiceTaxable = invoiceTaxable; }
    public BigDecimal getReturnTaxable() { return returnTaxable; }
    public void setReturnTaxable(BigDecimal returnTaxable) { this.returnTaxable = returnTaxable; }
    public BigDecimal getTaxableDifference() { return taxableDifference; }
    public void setTaxableDifference(BigDecimal taxableDifference) { this.taxableDifference = taxableDifference; }
    public BigDecimal getInvoiceTax() { return invoiceTax; }
    public void setInvoiceTax(BigDecimal invoiceTax) { this.invoiceTax = invoiceTax; }
    public BigDecimal getReturnTax() { return returnTax; }
    public void setReturnTax(BigDecimal returnTax) { this.returnTax = returnTax; }
    public BigDecimal getTaxDifference() { return taxDifference; }
    public void setTaxDifference(BigDecimal taxDifference) { this.taxDifference = taxDifference; }
}
