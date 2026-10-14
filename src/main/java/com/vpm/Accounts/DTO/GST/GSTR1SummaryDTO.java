package com.vpm.Accounts.DTO.GST;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GSTR1SummaryDTO {
    private LocalDate fromDate;
    private LocalDate toDate;

    private List<GSTR1InvoiceSummaryDTO> b2b;
    private List<GSTR1InvoiceSummaryDTO> b2cl;
    private List<GSTR1B2CSSummaryDTO> b2cs;
    private List<GSTR1ExportSummaryDTO> exports;
    private List<GSTR1NoteSummaryDTO> creditNotes;
    private List<GSTR1NoteSummaryDTO> debitNotes;
    private List<GSTR1HsnSummaryDTO> hsnSummary;

    private BigDecimal totalTaxableAmount = BigDecimal.ZERO;
    private BigDecimal totalCgst = BigDecimal.ZERO;
    private BigDecimal totalSgst = BigDecimal.ZERO;
    private BigDecimal totalIgst = BigDecimal.ZERO;
    private BigDecimal totalCess = BigDecimal.ZERO;
    private BigDecimal totalTax = BigDecimal.ZERO;

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public List<GSTR1InvoiceSummaryDTO> getB2b() { return b2b; }
    public void setB2b(List<GSTR1InvoiceSummaryDTO> b2b) { this.b2b = b2b; }
    public List<GSTR1InvoiceSummaryDTO> getB2cl() { return b2cl; }
    public void setB2cl(List<GSTR1InvoiceSummaryDTO> b2cl) { this.b2cl = b2cl; }
    public List<GSTR1B2CSSummaryDTO> getB2cs() { return b2cs; }
    public void setB2cs(List<GSTR1B2CSSummaryDTO> b2cs) { this.b2cs = b2cs; }
    public List<GSTR1ExportSummaryDTO> getExports() { return exports; }
    public void setExports(List<GSTR1ExportSummaryDTO> exports) { this.exports = exports; }
    public List<GSTR1NoteSummaryDTO> getCreditNotes() { return creditNotes; }
    public void setCreditNotes(List<GSTR1NoteSummaryDTO> creditNotes) { this.creditNotes = creditNotes; }
    public List<GSTR1NoteSummaryDTO> getDebitNotes() { return debitNotes; }
    public void setDebitNotes(List<GSTR1NoteSummaryDTO> debitNotes) { this.debitNotes = debitNotes; }
    public List<GSTR1HsnSummaryDTO> getHsnSummary() { return hsnSummary; }
    public void setHsnSummary(List<GSTR1HsnSummaryDTO> hsnSummary) { this.hsnSummary = hsnSummary; }

    public BigDecimal getTotalTaxableAmount() { return totalTaxableAmount; }
    public void setTotalTaxableAmount(BigDecimal totalTaxableAmount) { this.totalTaxableAmount = totalTaxableAmount; }
    public BigDecimal getTotalCgst() { return totalCgst; }
    public void setTotalCgst(BigDecimal totalCgst) { this.totalCgst = totalCgst; }
    public BigDecimal getTotalSgst() { return totalSgst; }
    public void setTotalSgst(BigDecimal totalSgst) { this.totalSgst = totalSgst; }
    public BigDecimal getTotalIgst() { return totalIgst; }
    public void setTotalIgst(BigDecimal totalIgst) { this.totalIgst = totalIgst; }
    public BigDecimal getTotalCess() { return totalCess; }
    public void setTotalCess(BigDecimal totalCess) { this.totalCess = totalCess; }
    public BigDecimal getTotalTax() { return totalTax; }
    public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }
}
