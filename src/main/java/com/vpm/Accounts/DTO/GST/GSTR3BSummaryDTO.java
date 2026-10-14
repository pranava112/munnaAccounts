package com.vpm.Accounts.DTO.GST;

import java.math.BigDecimal;

public class GSTR3BSummaryDTO {
    private int year;
    private int month;
    private BigDecimal outwardTaxable = BigDecimal.ZERO;
    private BigDecimal cgst = BigDecimal.ZERO;
    private BigDecimal sgst = BigDecimal.ZERO;
    private BigDecimal igst = BigDecimal.ZERO;
    private BigDecimal cess = BigDecimal.ZERO;
    private BigDecimal totalTax = BigDecimal.ZERO;
    private BigDecimal inputCgst = BigDecimal.ZERO;
    private BigDecimal inputSgst = BigDecimal.ZERO;
    private BigDecimal inputIgst = BigDecimal.ZERO;
    private BigDecimal inputCess = BigDecimal.ZERO;
    private BigDecimal itcReversal = BigDecimal.ZERO;
    private BigDecimal reverseCharge = BigDecimal.ZERO;
    private BigDecimal taxPayable = BigDecimal.ZERO;
    private BigDecimal taxPaid = BigDecimal.ZERO;
    private String status;

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public BigDecimal getOutwardTaxable() { return outwardTaxable; }
    public void setOutwardTaxable(BigDecimal outwardTaxable) { this.outwardTaxable = outwardTaxable; }
    public BigDecimal getCgst() { return cgst; }
    public void setCgst(BigDecimal cgst) { this.cgst = cgst; }
    public BigDecimal getSgst() { return sgst; }
    public void setSgst(BigDecimal sgst) { this.sgst = sgst; }
    public BigDecimal getIgst() { return igst; }
    public void setIgst(BigDecimal igst) { this.igst = igst; }
    public BigDecimal getCess() { return cess; }
    public void setCess(BigDecimal cess) { this.cess = cess; }
    public BigDecimal getTotalTax() { return totalTax; }
    public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }
    public BigDecimal getInputCgst() { return inputCgst; }
    public void setInputCgst(BigDecimal inputCgst) { this.inputCgst = inputCgst; }
    public BigDecimal getInputSgst() { return inputSgst; }
    public void setInputSgst(BigDecimal inputSgst) { this.inputSgst = inputSgst; }
    public BigDecimal getInputIgst() { return inputIgst; }
    public void setInputIgst(BigDecimal inputIgst) { this.inputIgst = inputIgst; }
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
}
