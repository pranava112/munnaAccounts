package com.vpm.Accounts.DTO.AI;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AITransactionDraftDTO {

    private String transactionType;
    private LocalDate date;
    private String counterpartyName;
    private BigDecimal invoiceTotal;
    private String confidence;
    private List<String> warnings;
    private List<AITransactionItemDTO> items;

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getCounterpartyName() { return counterpartyName; }
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }
    public BigDecimal getInvoiceTotal() { return invoiceTotal; }
    public void setInvoiceTotal(BigDecimal invoiceTotal) { this.invoiceTotal = invoiceTotal; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public List<AITransactionItemDTO> getItems() { return items; }
    public void setItems(List<AITransactionItemDTO> items) { this.items = items; }
}
