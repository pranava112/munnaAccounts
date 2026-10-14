package com.vpm.Accounts.Service.AI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.Accounts.DTO.AI.AIJournalDraftDTO;
import com.vpm.Accounts.DTO.AI.AIJournalLineDTO;

class OpenAIServiceValidationTest {

    private final OpenAIService service = new OpenAIService(
            RestClient.create(),
            new ObjectMapper(),
            "gpt-4.1");

    @Test
    void acceptsBalancedDraftWithValidAccountReferences() {
        AIJournalDraftDTO draft = draft(
                line(12L, "Purchases", "100.00", "0.00"),
                line(18L, "Accounts Payable", "0.00", "100.00"));

        assertDoesNotThrow(() -> service.validateJournalDraft(draft));
    }

    @Test
    void rejectsLineWithBothDebitAndCredit() {
        AIJournalDraftDTO draft = draft(
                line(12L, "Purchases", "100.00", "100.00"),
                line(18L, "Accounts Payable", "0.00", "0.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateJournalDraft(draft));
    }

    @Test
    void rejectsMissingAccountIdBeforePosting() {
        AIJournalDraftDTO draft = draft(
                line(null, "Purchases", "100.00", "0.00"),
                line(18L, "Accounts Payable", "0.00", "100.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateJournalDraft(draft));
    }

    private static AIJournalDraftDTO draft(AIJournalLineDTO... lines) {
        AIJournalDraftDTO draft = new AIJournalDraftDTO();
        draft.setDate(LocalDate.of(2026, 4, 1));
        draft.setVoucherType("PURCHASE");
        draft.setNarration("Test purchase invoice");
        draft.setLines(List.of(lines));
        return draft;
    }

    private static AIJournalLineDTO line(
            Long accountId,
            String accountName,
            String debit,
            String credit) {
        AIJournalLineDTO line = new AIJournalLineDTO();
        line.setAccountId(accountId);
        line.setAccountName(accountName);
        line.setDebit(new BigDecimal(debit));
        line.setCredit(new BigDecimal(credit));
        return line;
    }
}
