package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Entity.GST.TaxInvoice;
import com.vpm.Accounts.Enum.InvoiceStatus;
import com.vpm.Accounts.Repository.GST.CreditDebitNoteRepository;
import com.vpm.Accounts.Repository.GST.TaxInvoiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReturnProtectionService {
    private final TaxInvoiceRepository invoiceRepository;
    private final CreditDebitNoteRepository noteRepository;

    public ReturnProtectionService(
            TaxInvoiceRepository invoiceRepository,
            CreditDebitNoteRepository noteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public Set<Long> protectedSourceIds(String sourceType, Collection<Long> sourceIds) {
        String normalizedSourceType = sourceType == null ? "" : sourceType.trim().toUpperCase();
        if (!"SALE".equals(normalizedSourceType) && !"PURCHASE".equals(normalizedSourceType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "sourceType must be SALE or PURCHASE");
        }
        if (sourceIds == null || sourceIds.isEmpty()) {
            return Set.of();
        }

        List<TaxInvoice> invoices = invoiceRepository.findBySourceTypeAndSourceIdIn(
                normalizedSourceType, sourceIds);
        List<Long> invoiceIds = invoices.stream()
                .map(TaxInvoice::getId)
                .toList();
        if (invoiceIds.isEmpty()) {
            return Set.of();
        }

        Set<Long> adjustedInvoiceIds = noteRepository.findByOriginalInvoiceIdIn(invoiceIds).stream()
                .filter(note -> note.getStatus() != InvoiceStatus.CANCELLED)
                .map(CreditDebitNote::getOriginalInvoiceId)
                .collect(Collectors.toSet());

        return invoices.stream()
                .filter(invoice -> adjustedInvoiceIds.contains(invoice.getId()))
                .map(TaxInvoice::getSourceId)
                .collect(Collectors.toSet());
    }
}
