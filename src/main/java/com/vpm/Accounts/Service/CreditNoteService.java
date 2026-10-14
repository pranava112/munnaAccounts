package com.vpm.Accounts.Service;

import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Enum.NoteType;
import com.vpm.Accounts.Service.GST.TaxApiService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CreditNoteService {
    private final TaxApiService taxApiService;

    public CreditNoteService(TaxApiService taxApiService) {
        this.taxApiService = taxApiService;
    }

    @Transactional(readOnly = true)
    public List<CreditDebitNote> findAll() {
        return taxApiService.notes().stream()
                .filter(CreditNoteService::isCreditNote)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditDebitNote findById(Long id) {
        return requireCreditNote(taxApiService.note(id));
    }

    @Transactional
    public CreditDebitNote create(CreditDebitNote note) {
        if (note == null) {
            throw badRequest("Credit note details are required");
        }
        if (note.getTransactionType() != null
                && !"SALES".equalsIgnoreCase(note.getTransactionType().trim())) {
            throw badRequest("Credit notes can only be created for sales returns");
        }
        if (note.getNoteType() != null
                && note.getNoteType() != NoteType.CREDIT
                && note.getNoteType() != NoteType.CREDIT_NOTE) {
            throw badRequest("Sales returns require a credit note");
        }

        note.setTransactionType("SALES");
        note.setNoteType(NoteType.CREDIT);
        return taxApiService.createNote(note);
    }

    @Transactional
    public CreditDebitNote confirm(Long id) {
        findById(id);
        return taxApiService.confirmNote(id);
    }

    @Transactional
    public CreditDebitNote cancel(Long id) {
        findById(id);
        return taxApiService.cancelNote(id);
    }

    private static boolean isCreditNote(CreditDebitNote note) {
        return "SALES".equalsIgnoreCase(note.getTransactionType())
                && (note.getNoteType() == NoteType.CREDIT
                || note.getNoteType() == NoteType.CREDIT_NOTE);
    }

    private static CreditDebitNote requireCreditNote(CreditDebitNote note) {
        if (!isCreditNote(note)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Credit note not found");
        }
        return note;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
