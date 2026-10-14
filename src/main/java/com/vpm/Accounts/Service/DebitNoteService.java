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
public class DebitNoteService {
    private final TaxApiService taxApiService;

    public DebitNoteService(TaxApiService taxApiService) {
        this.taxApiService = taxApiService;
    }

    @Transactional(readOnly = true)
    public List<CreditDebitNote> findAll() {
        return taxApiService.notes().stream()
                .filter(DebitNoteService::isDebitNote)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditDebitNote findById(Long id) {
        return requireDebitNote(taxApiService.note(id));
    }

    @Transactional
    public CreditDebitNote create(CreditDebitNote note) {
        if (note == null) {
            throw badRequest("Debit note details are required");
        }
        if (note.getTransactionType() != null
                && !"PURCHASE".equalsIgnoreCase(note.getTransactionType().trim())) {
            throw badRequest("Debit notes can only be created for purchase returns");
        }
        if (note.getNoteType() != null
                && note.getNoteType() != NoteType.DEBIT
                && note.getNoteType() != NoteType.DEBIT_NOTE) {
            throw badRequest("Purchase returns require a debit note");
        }

        note.setTransactionType("PURCHASE");
        note.setNoteType(NoteType.DEBIT);
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

    private static boolean isDebitNote(CreditDebitNote note) {
        return "PURCHASE".equalsIgnoreCase(note.getTransactionType())
                && (note.getNoteType() == NoteType.DEBIT
                || note.getNoteType() == NoteType.DEBIT_NOTE);
    }

    private static CreditDebitNote requireDebitNote(CreditDebitNote note) {
        if (!isDebitNote(note)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Debit note not found");
        }
        return note;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
