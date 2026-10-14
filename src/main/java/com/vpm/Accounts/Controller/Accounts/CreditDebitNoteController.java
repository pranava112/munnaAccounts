package com.vpm.Accounts.Controller.Accounts;

import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Service.CreditNoteService;
import com.vpm.Accounts.Service.DebitNoteService;
import com.vpm.Accounts.Service.GST.ReturnProtectionService;
import com.vpm.Accounts.Service.GST.TaxApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class CreditDebitNoteController {
    private final CreditNoteService creditNoteService;
    private final DebitNoteService debitNoteService;
    private final TaxApiService taxApiService;
    private final ReturnProtectionService returnProtectionService;

    public CreditDebitNoteController(
            CreditNoteService creditNoteService,
            DebitNoteService debitNoteService,
            TaxApiService taxApiService,
            ReturnProtectionService returnProtectionService) {
        this.creditNoteService = creditNoteService;
        this.debitNoteService = debitNoteService;
        this.taxApiService = taxApiService;
        this.returnProtectionService = returnProtectionService;
    }

    @GetMapping("/api/returns/protected-sources")
    public ResponseEntity<Set<Long>> protectedSources(
            @org.springframework.web.bind.annotation.RequestParam String sourceType,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Collection<Long> sourceIds) {
        return ResponseEntity.ok(
                returnProtectionService.protectedSourceIds(sourceType, sourceIds));
    }

    @GetMapping("/api/credit-debit-notes")
    public ResponseEntity<List<CreditDebitNote>> findAllNotes() {
        List<CreditDebitNote> notes = taxApiService.notes().stream()
                .sorted(Comparator.comparing(
                        CreditDebitNote::getNoteDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/api/credit-notes")
    public ResponseEntity<CreditDebitNote> createCreditNote(@RequestBody CreditDebitNote note) {
        return ResponseEntity.status(201).body(creditNoteService.create(note));
    }

    @GetMapping("/api/credit-notes")
    public ResponseEntity<List<CreditDebitNote>> findCreditNotes() {
        return ResponseEntity.ok(creditNoteService.findAll());
    }

    @GetMapping("/api/credit-notes/{id}")
    public ResponseEntity<CreditDebitNote> findCreditNote(@PathVariable Long id) {
        return ResponseEntity.ok(creditNoteService.findById(id));
    }

    @PostMapping("/api/credit-notes/{id}/confirm")
    public ResponseEntity<CreditDebitNote> confirmCreditNote(@PathVariable Long id) {
        return ResponseEntity.ok(creditNoteService.confirm(id));
    }

    @PostMapping("/api/credit-notes/{id}/cancel")
    public ResponseEntity<CreditDebitNote> cancelCreditNote(@PathVariable Long id) {
        return ResponseEntity.ok(creditNoteService.cancel(id));
    }

    @PostMapping("/api/debit-notes")
    public ResponseEntity<CreditDebitNote> createDebitNote(@RequestBody CreditDebitNote note) {
        return ResponseEntity.status(201).body(debitNoteService.create(note));
    }

    @GetMapping("/api/debit-notes")
    public ResponseEntity<List<CreditDebitNote>> findDebitNotes() {
        return ResponseEntity.ok(debitNoteService.findAll());
    }

    @GetMapping("/api/debit-notes/{id}")
    public ResponseEntity<CreditDebitNote> findDebitNote(@PathVariable Long id) {
        return ResponseEntity.ok(debitNoteService.findById(id));
    }

    @PostMapping("/api/debit-notes/{id}/confirm")
    public ResponseEntity<CreditDebitNote> confirmDebitNote(@PathVariable Long id) {
        return ResponseEntity.ok(debitNoteService.confirm(id));
    }

    @PostMapping("/api/debit-notes/{id}/cancel")
    public ResponseEntity<CreditDebitNote> cancelDebitNote(@PathVariable Long id) {
        return ResponseEntity.ok(debitNoteService.cancel(id));
    }
}
