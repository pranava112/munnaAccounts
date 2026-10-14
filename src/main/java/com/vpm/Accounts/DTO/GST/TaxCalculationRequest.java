package com.vpm.Accounts.DTO.GST;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxCalculationRequest {
    private BigDecimal amount;
    private BigDecimal rate;
    private LocalDate transactionDate;
    private String reference;
    private String partyName;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }
}
