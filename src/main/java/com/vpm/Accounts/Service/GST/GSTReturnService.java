package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.DTO.GST.GSTR1B2CSSummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTR1ExportSummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTR1HsnSummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTR1InvoiceSummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTR1NoteSummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTR1SummaryDTO;
import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Entity.GST.TaxInvoice;
import com.vpm.Accounts.Entity.GST.TaxInvoiceLine;
import com.vpm.Accounts.Entity.GST.CreditDebitNoteLine;
import com.vpm.Accounts.Enum.InvoiceStatus;
import com.vpm.Accounts.Enum.NoteType;
import com.vpm.Accounts.Repository.GST.CreditDebitNoteRepository;
import com.vpm.Accounts.Repository.GST.TaxInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

@Service
public class GSTReturnService {

    private static final BigDecimal B2CL_THRESHOLD = new BigDecimal("100000");
    private static final Collection<InvoiceStatus> RETURNABLE_STATUSES = List.of(
            InvoiceStatus.CONFIRMED,
            InvoiceStatus.E_INVOICE_GENERATED,
            InvoiceStatus.E_WAY_BILL_GENERATED);

    private final TaxInvoiceRepository invoiceRepository;
    private final CreditDebitNoteRepository noteRepository;

    public GSTReturnService(TaxInvoiceRepository invoiceRepository,
                            CreditDebitNoteRepository noteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public GSTR1SummaryDTO generateGSTR1(LocalDate from, LocalDate to) {
        List<TaxInvoice> invoices = invoiceRepository
                .findByTransactionTypeAndInvoiceDateBetweenAndStatusIn(
                        "SALES", from, to, RETURNABLE_STATUSES);

        List<CreditDebitNote> notes = noteRepository
                .findByNoteDateBetweenAndStatus(from, to, InvoiceStatus.CONFIRMED)
                .stream()
                .filter(note -> note.getTransactionType() == null
                        || "SALES".equalsIgnoreCase(note.getTransactionType()))
                .toList();

        List<GSTR1InvoiceSummaryDTO> b2bInvoices = new ArrayList<>();
        List<GSTR1InvoiceSummaryDTO> b2clInvoices = new ArrayList<>();
        List<GSTR1B2CSSummaryDTO> b2csList = new ArrayList<>();
        List<GSTR1ExportSummaryDTO> exportInvoices = new ArrayList<>();
        Map<String, GSTR1B2CSSummaryDTO> b2csAggregator = new HashMap<>();

        for (TaxInvoice inv : invoices) {
            boolean isExport = isExportInvoice(inv);
            boolean hasGSTIN = inv.getCustomerGSTIN() != null && !inv.getCustomerGSTIN().trim().isEmpty();

            if (isExport) {
                exportInvoices.add(mapToExportSummary(inv));
            } else if (hasGSTIN) {
                b2bInvoices.add(mapToInvoiceSummary(inv));
            } else {
                BigDecimal taxable = safe(inv.getTaxableAmount());
                boolean isInterState = isInterStateSupply(inv);
                if (isInterState && taxable.compareTo(B2CL_THRESHOLD) > 0) {
                    b2clInvoices.add(mapToInvoiceSummary(inv));
                } else {
                    aggregateB2CS(inv, b2csAggregator);
                }
            }
        }
        b2csList.addAll(b2csAggregator.values());

        List<GSTR1NoteSummaryDTO> creditNotes = new ArrayList<>();
        List<GSTR1NoteSummaryDTO> debitNotes = new ArrayList<>();
        for (CreditDebitNote note : notes) {
            GSTR1NoteSummaryDTO noteDto = mapToNoteSummary(note);
            if (note.getNoteType() == NoteType.CREDIT || note.getNoteType() == NoteType.CREDIT_NOTE) {
                creditNotes.add(noteDto);
            } else if (note.getNoteType() == NoteType.DEBIT || note.getNoteType() == NoteType.DEBIT_NOTE) {
                debitNotes.add(noteDto);
            }
        }

        List<GSTR1HsnSummaryDTO> hsnSummary = aggregateHsn(invoices, notes);

        GSTR1SummaryDTO summary = new GSTR1SummaryDTO();
        summary.setFromDate(from);
        summary.setToDate(to);
        summary.setB2b(b2bInvoices);
        summary.setB2cl(b2clInvoices);
        summary.setB2cs(b2csList);
        summary.setExports(exportInvoices);
        summary.setCreditNotes(creditNotes);
        summary.setDebitNotes(debitNotes);
        summary.setHsnSummary(hsnSummary);
        calculateOverallTotals(summary);
        return summary;
    }

    private boolean isExportInvoice(TaxInvoice invoice) {
        String pos = invoice.getPlaceOfSupply();
        return (pos != null && pos.equalsIgnoreCase("96"))
                || "EXPORT".equalsIgnoreCase(invoice.getCustomerStateCode());
    }

    private boolean isInterStateSupply(TaxInvoice invoice) {
        String supplierGstin = invoice.getSupplierGSTIN();
        String customerStateCode = invoice.getCustomerStateCode();
        if (supplierGstin != null && supplierGstin.length() >= 2
                && customerStateCode != null && !customerStateCode.isBlank()) {
            return !supplierGstin.substring(0, 2).equals(customerStateCode.trim());
        }
        return safe(invoice.getIgst()).compareTo(BigDecimal.ZERO) > 0;
    }

    private GSTR1InvoiceSummaryDTO mapToInvoiceSummary(TaxInvoice inv) {
        GSTR1InvoiceSummaryDTO dto = new GSTR1InvoiceSummaryDTO();
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setInvoiceDate(inv.getInvoiceDate());
        dto.setCustomerName(inv.getCustomerName());
        dto.setCustomerGstin(inv.getCustomerGSTIN());
        dto.setPlaceOfSupply(inv.getPlaceOfSupply());
        dto.setTaxableAmount(safe(inv.getTaxableAmount()));
        dto.setCgst(safe(inv.getCgst()));
        dto.setSgst(safe(inv.getSgst()));
        dto.setIgst(safe(inv.getIgst()));
        dto.setCess(safe(inv.getCess()));
        dto.setTotalTax(safe(inv.getTotalTax()));
        dto.setGrandTotal(safe(inv.getGrandTotal()));
        return dto;
    }

    private GSTR1ExportSummaryDTO mapToExportSummary(TaxInvoice inv) {
        GSTR1ExportSummaryDTO dto = new GSTR1ExportSummaryDTO();
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setInvoiceDate(inv.getInvoiceDate());
        dto.setTaxableAmount(safe(inv.getTaxableAmount()));
        dto.setIgst(safe(inv.getIgst()));
        dto.setCess(safe(inv.getCess()));
        dto.setGrandTotal(safe(inv.getGrandTotal()));
        return dto;
    }

    private GSTR1NoteSummaryDTO mapToNoteSummary(CreditDebitNote note) {
        GSTR1NoteSummaryDTO dto = new GSTR1NoteSummaryDTO();
        dto.setNoteNumber(note.getNoteNumber());
        dto.setNoteDate(note.getNoteDate());
        dto.setNoteType(note.getNoteType());
        dto.setOriginalInvoiceId(note.getOriginalInvoiceId());
        dto.setTaxableAmount(safe(note.getTaxableAmount()));
        dto.setCgst(safe(note.getCgst()));
        dto.setSgst(safe(note.getSgst()));
        dto.setIgst(safe(note.getIgst()));
        dto.setCess(safe(note.getCess()));
        dto.setTotal(safe(note.getTotal()));
        dto.setReason(note.getReason());
        return dto;
    }

    private void aggregateB2CS(TaxInvoice inv, Map<String, GSTR1B2CSSummaryDTO> b2csAggregator) {
        String pos = inv.getPlaceOfSupply() != null ? inv.getPlaceOfSupply() : "OTHER";

        if (inv.getLines() != null && !inv.getLines().isEmpty()) {
            for (TaxInvoiceLine line : inv.getLines()) {
                BigDecimal rate = safe(line.getGstRate());
                String key = pos + "_" + rate.stripTrailingZeros().toPlainString();

                GSTR1B2CSSummaryDTO dto = b2csAggregator.computeIfAbsent(key, k -> {
                    GSTR1B2CSSummaryDTO item = new GSTR1B2CSSummaryDTO();
                    item.setPlaceOfSupply(pos);
                    item.setRate(rate);
                    item.setTaxableAmount(BigDecimal.ZERO);
                    item.setCgst(BigDecimal.ZERO);
                    item.setSgst(BigDecimal.ZERO);
                    item.setIgst(BigDecimal.ZERO);
                    item.setCess(BigDecimal.ZERO);
                    return item;
                });

                dto.setTaxableAmount(dto.getTaxableAmount().add(safe(line.getTaxableAmount())));
                dto.setCgst(dto.getCgst().add(safe(line.getCgst())));
                dto.setSgst(dto.getSgst().add(safe(line.getSgst())));
                dto.setIgst(dto.getIgst().add(safe(line.getIgst())));
                dto.setCess(dto.getCess().add(safe(line.getCess())));
            }
        } else {
            String key = pos + "_DEFAULT";
            GSTR1B2CSSummaryDTO dto = b2csAggregator.computeIfAbsent(key, k -> {
                GSTR1B2CSSummaryDTO item = new GSTR1B2CSSummaryDTO();
                item.setPlaceOfSupply(pos);
                item.setTaxableAmount(BigDecimal.ZERO);
                item.setCgst(BigDecimal.ZERO);
                item.setSgst(BigDecimal.ZERO);
                item.setIgst(BigDecimal.ZERO);
                item.setCess(BigDecimal.ZERO);
                return item;
            });

            dto.setTaxableAmount(dto.getTaxableAmount().add(safe(inv.getTaxableAmount())));
            dto.setCgst(dto.getCgst().add(safe(inv.getCgst())));
            dto.setSgst(dto.getSgst().add(safe(inv.getSgst())));
            dto.setIgst(dto.getIgst().add(safe(inv.getIgst())));
            dto.setCess(dto.getCess().add(safe(inv.getCess())));
        }
    }

    private List<GSTR1HsnSummaryDTO> aggregateHsn(
            List<TaxInvoice> invoices, List<CreditDebitNote> notes) {
        Map<String, GSTR1HsnSummaryDTO> map = new HashMap<>();

        for (TaxInvoice inv : invoices) {
            if (inv.getLines() == null) continue;
            for (TaxInvoiceLine line : inv.getLines()) {
                String code = line.getHsnSacCode() != null ? line.getHsnSacCode().trim() : "UNKNOWN";
                BigDecimal rate = safe(line.getGstRate());
                String key = code + "_" + rate.stripTrailingZeros().toPlainString();

                GSTR1HsnSummaryDTO summary = map.computeIfAbsent(key, k -> {
                    GSTR1HsnSummaryDTO dto = new GSTR1HsnSummaryDTO();
                    dto.setHsnSacCode(code);
                    dto.setDescription(line.getDescription());
                    dto.setGstRate(rate);
                    dto.setTotalQuantity(BigDecimal.ZERO);
                    dto.setTaxableAmount(BigDecimal.ZERO);
                    dto.setCgst(BigDecimal.ZERO);
                    dto.setSgst(BigDecimal.ZERO);
                    dto.setIgst(BigDecimal.ZERO);
                    dto.setCess(BigDecimal.ZERO);
                    dto.setTotalTax(BigDecimal.ZERO);
                    return dto;
                });

                summary.setTotalQuantity(summary.getTotalQuantity().add(safe(line.getQuantity())));
                summary.setTaxableAmount(summary.getTaxableAmount().add(safe(line.getTaxableAmount())));
                summary.setCgst(summary.getCgst().add(safe(line.getCgst())));
                summary.setSgst(summary.getSgst().add(safe(line.getSgst())));
                summary.setIgst(summary.getIgst().add(safe(line.getIgst())));
                summary.setCess(summary.getCess().add(safe(line.getCess())));

                BigDecimal lineTotalTax = safe(line.getCgst())
                        .add(safe(line.getSgst()))
                        .add(safe(line.getIgst()))
                        .add(safe(line.getCess()));
                summary.setTotalTax(summary.getTotalTax().add(lineTotalTax));
            }
        }

        for (CreditDebitNote note : notes) {
            if (note.getLines() == null) continue;
            BigDecimal direction = isCreditNote(note)
                    ? BigDecimal.ONE.negate() : BigDecimal.ONE;
            for (CreditDebitNoteLine line : note.getLines()) {
                String code = line.getHsnSacCode() == null ? "UNKNOWN" : line.getHsnSacCode().trim();
                BigDecimal rate = safe(line.getGstRate());
                String key = code + "_" + rate.stripTrailingZeros().toPlainString();
                GSTR1HsnSummaryDTO summary = map.computeIfAbsent(key, ignored -> {
                    GSTR1HsnSummaryDTO dto = new GSTR1HsnSummaryDTO();
                    dto.setHsnSacCode(code);
                    dto.setDescription(line.getDescription());
                    dto.setGstRate(rate);
                    dto.setTotalQuantity(BigDecimal.ZERO);
                    dto.setTaxableAmount(BigDecimal.ZERO);
                    dto.setCgst(BigDecimal.ZERO);
                    dto.setSgst(BigDecimal.ZERO);
                    dto.setIgst(BigDecimal.ZERO);
                    dto.setCess(BigDecimal.ZERO);
                    dto.setTotalTax(BigDecimal.ZERO);
                    return dto;
                });
                summary.setTotalQuantity(summary.getTotalQuantity()
                        .add(safe(line.getQuantity()).multiply(direction)));
                summary.setTaxableAmount(summary.getTaxableAmount()
                        .add(safe(line.getTaxableAmount()).multiply(direction)));
                summary.setCgst(summary.getCgst().add(safe(line.getCgst()).multiply(direction)));
                summary.setSgst(summary.getSgst().add(safe(line.getSgst()).multiply(direction)));
                summary.setIgst(summary.getIgst().add(safe(line.getIgst()).multiply(direction)));
                summary.setCess(summary.getCess().add(safe(line.getCess()).multiply(direction)));
                summary.setTotalTax(summary.getTotalTax().add(safe(line.getCgst())
                        .add(safe(line.getSgst())).add(safe(line.getIgst()))
                        .add(safe(line.getCess())).multiply(direction)));
            }
        }

        return new ArrayList<>(map.values());
    }

    private boolean isCreditNote(CreditDebitNote note) {
        return note.getNoteType() == NoteType.CREDIT || note.getNoteType() == NoteType.CREDIT_NOTE;
    }

    private void calculateOverallTotals(GSTR1SummaryDTO summary) {
        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalCess = BigDecimal.ZERO;

        if (summary.getB2b() != null) {
            for (GSTR1InvoiceSummaryDTO inv : summary.getB2b()) {
                totalTaxable = totalTaxable.add(inv.getTaxableAmount());
                totalCgst = totalCgst.add(inv.getCgst());
                totalSgst = totalSgst.add(inv.getSgst());
                totalIgst = totalIgst.add(inv.getIgst());
                totalCess = totalCess.add(inv.getCess());
            }
        }
        if (summary.getB2cl() != null) {
            for (GSTR1InvoiceSummaryDTO inv : summary.getB2cl()) {
                totalTaxable = totalTaxable.add(inv.getTaxableAmount());
                totalCgst = totalCgst.add(inv.getCgst());
                totalSgst = totalSgst.add(inv.getSgst());
                totalIgst = totalIgst.add(inv.getIgst());
                totalCess = totalCess.add(inv.getCess());
            }
        }
        if (summary.getB2cs() != null) {
            for (GSTR1B2CSSummaryDTO inv : summary.getB2cs()) {
                totalTaxable = totalTaxable.add(inv.getTaxableAmount());
                totalCgst = totalCgst.add(inv.getCgst());
                totalSgst = totalSgst.add(inv.getSgst());
                totalIgst = totalIgst.add(inv.getIgst());
                totalCess = totalCess.add(inv.getCess());
            }
        }
        if (summary.getExports() != null) {
            for (GSTR1ExportSummaryDTO exp : summary.getExports()) {
                totalTaxable = totalTaxable.add(exp.getTaxableAmount());
                totalIgst = totalIgst.add(exp.getIgst());
                totalCess = totalCess.add(exp.getCess());
            }
        }
        if (summary.getDebitNotes() != null) {
            for (GSTR1NoteSummaryDTO dn : summary.getDebitNotes()) {
                totalTaxable = totalTaxable.add(dn.getTaxableAmount());
                totalCgst = totalCgst.add(dn.getCgst());
                totalSgst = totalSgst.add(dn.getSgst());
                totalIgst = totalIgst.add(dn.getIgst());
                totalCess = totalCess.add(dn.getCess());
            }
        }
        if (summary.getCreditNotes() != null) {
            for (GSTR1NoteSummaryDTO cn : summary.getCreditNotes()) {
                totalTaxable = totalTaxable.subtract(cn.getTaxableAmount());
                totalCgst = totalCgst.subtract(cn.getCgst());
                totalSgst = totalSgst.subtract(cn.getSgst());
                totalIgst = totalIgst.subtract(cn.getIgst());
                totalCess = totalCess.subtract(cn.getCess());
            }
        }

        summary.setTotalTaxableAmount(totalTaxable);
        summary.setTotalCgst(totalCgst);
        summary.setTotalSgst(totalSgst);
        summary.setTotalIgst(totalIgst);
        summary.setTotalCess(totalCess);
        summary.setTotalTax(totalCgst.add(totalSgst).add(totalIgst).add(totalCess));
    }

    private BigDecimal safe(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}
