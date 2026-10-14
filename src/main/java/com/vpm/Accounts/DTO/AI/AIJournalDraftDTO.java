// package com.vpm.Accounts.DTO;

package com.vpm.Accounts.DTO.AI;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// import java.math.BigDecimal;

public class AIJournalDraftDTO {

    private LocalDate date;

    private String voucherType;

    private String narration;

    private BigDecimal totalDebit;

    private BigDecimal totalCredit;

    private boolean balanced;

    private List<AIJournalLineDTO> lines = new ArrayList<>();

    private String confidence;

    private List<String> warnings = new ArrayList<>();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public boolean isBalanced() {
        return balanced;
    }

    public void setBalanced(boolean balanced) {
        this.balanced = balanced;
    }

    public List<AIJournalLineDTO> getLines() {
        return lines;
    }

    public void setLines(List<AIJournalLineDTO> lines) {
        this.lines = lines;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}