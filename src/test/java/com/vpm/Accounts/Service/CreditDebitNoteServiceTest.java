package com.vpm.Accounts.Service;

import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Enum.NoteType;
import com.vpm.Accounts.Service.GST.TaxApiService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditDebitNoteServiceTest {
    private final TaxApiService taxApiService = mock(TaxApiService.class);
    private final CreditNoteService creditNoteService = new CreditNoteService(taxApiService);
    private final DebitNoteService debitNoteService = new DebitNoteService(taxApiService);

    @Test
    void creditServiceCreatesSalesCreditNote() {
        CreditDebitNote note = new CreditDebitNote();
        note.setTransactionType("SALES");
        when(taxApiService.createNote(note)).thenReturn(note);

        assertEquals(note, creditNoteService.create(note));
        assertEquals("SALES", note.getTransactionType());
        assertEquals(NoteType.CREDIT, note.getNoteType());
        verify(taxApiService).createNote(note);
    }

    @Test
    void debitServiceCreatesPurchaseDebitNote() {
        CreditDebitNote note = new CreditDebitNote();
        note.setTransactionType("PURCHASE");
        when(taxApiService.createNote(note)).thenReturn(note);

        assertEquals(note, debitNoteService.create(note));
        assertEquals("PURCHASE", note.getTransactionType());
        assertEquals(NoteType.DEBIT, note.getNoteType());
        verify(taxApiService).createNote(note);
    }

    @Test
    void servicesRejectNotesForTheWrongTransactionDirection() {
        CreditDebitNote purchase = new CreditDebitNote();
        purchase.setTransactionType("PURCHASE");
        CreditDebitNote sales = new CreditDebitNote();
        sales.setTransactionType("SALES");

        ResponseStatusException creditError =
                assertThrows(ResponseStatusException.class, () -> creditNoteService.create(purchase));
        ResponseStatusException debitError =
                assertThrows(ResponseStatusException.class, () -> debitNoteService.create(sales));

        assertEquals(HttpStatus.BAD_REQUEST, creditError.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, debitError.getStatusCode());
        verify(taxApiService, never()).createNote(any());
    }

    @Test
    void noteListsOnlyReturnTheirOwnNoteType() {
        CreditDebitNote credit = new CreditDebitNote();
        credit.setTransactionType("SALES");
        credit.setNoteType(NoteType.CREDIT);
        CreditDebitNote debit = new CreditDebitNote();
        debit.setTransactionType("PURCHASE");
        debit.setNoteType(NoteType.DEBIT);
        when(taxApiService.notes()).thenReturn(List.of(credit, debit));

        assertEquals(List.of(credit), creditNoteService.findAll());
        assertEquals(List.of(debit), debitNoteService.findAll());
    }

    @Test
    void lookingUpWrongNoteKindReturnsNotFound() {
        CreditDebitNote debit = new CreditDebitNote();
        debit.setTransactionType("PURCHASE");
        debit.setNoteType(NoteType.DEBIT);
        when(taxApiService.note(5L)).thenReturn(debit);

        ResponseStatusException error =
                assertThrows(ResponseStatusException.class, () -> creditNoteService.findById(5L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
