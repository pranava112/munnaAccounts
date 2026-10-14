package com.vpm.Accounts.Service.AI;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.DTO.AI.AIJournalDraftDTO;
import com.vpm.Accounts.DTO.AI.AIJournalLineDTO;
import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Service.JournalEntryService;

@Service
public class AIJournalPostingService {

    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final OpenAIService openAIService;

    public AIJournalPostingService(
            AccountRepository accountRepository,
            JournalEntryService journalEntryService,
            OpenAIService openAIService) {
        this.accountRepository = accountRepository;
        this.journalEntryService = journalEntryService;
        this.openAIService = openAIService;
    }

    @Transactional
    public JournalEntry post(
            AIJournalDraftDTO draft) {

        openAIService.validateJournalDraft(draft);

        return journalEntryService.saveJournalEntry(toJournal(draft));
    }

    @Transactional
    public List<JournalEntry> postBatch(List<AIJournalDraftDTO> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            throw new IllegalArgumentException("At least one journal is required");
        }

        drafts.forEach(openAIService::validateJournalDraft);

        List<JournalEntry> savedJournals = new ArrayList<>(drafts.size());
        for (AIJournalDraftDTO draft : drafts) {
            savedJournals.add(journalEntryService.saveJournalEntry(toJournal(draft)));
        }
        return savedJournals;
    }

    private JournalEntry toJournal(AIJournalDraftDTO draft) {
        JournalEntry journal =
                new JournalEntry();

        journal.setEntryDate(
                draft.getDate());
        journal.setDescription(draft.getNarration());
        journal.setVoucherType(draft.getVoucherType());
        journal.setLines(new ArrayList<>());

        for (AIJournalLineDTO dto :
                draft.getLines()) {

            if (dto.getAccountId() == null) {
                throw new IllegalArgumentException(
                        "Account ID is required for "
                                + dto.getAccountName());
            }

            Account account =
                    accountRepository
                            .findById(dto.getAccountId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Account not found: "
                                            + dto.getAccountId()));

            JournalEntryLine line =
                    new JournalEntryLine();

            line.setJournalEntry(
                    journal);

            line.setAccount(account);

            line.setDebit(
                    dto.getDebit());

            line.setCredit(
                    dto.getCredit());

            journal.getLines().add(line);
        }

        return journal;
    }
}