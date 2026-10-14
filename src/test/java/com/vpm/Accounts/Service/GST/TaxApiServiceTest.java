package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.Entity.GST.GSTRegistration;
import com.vpm.Accounts.Entity.GST.TenantTaxSettings;
import com.vpm.Accounts.Entity.GST.TaxInvoice;
import com.vpm.Accounts.Entity.GST.TaxInvoiceLine;
import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Entity.GST.CreditDebitNoteLine;
import com.vpm.Accounts.Enum.GSTRegistrationType;
import com.vpm.Accounts.Enum.InvoiceStatus;
import com.vpm.Accounts.Enum.NoteType;
import com.vpm.Accounts.Repository.GST.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaxApiServiceTest {
    private final GSTRegistrationRepository registrationRepository = mock(GSTRegistrationRepository.class);
    private final TaxInvoiceRepository invoiceRepository = mock(TaxInvoiceRepository.class);
    private final CreditDebitNoteRepository noteRepository = mock(CreditDebitNoteRepository.class);
    private final HsnSacCodeRepository hsnRepository = mock(HsnSacCodeRepository.class);
    private final TaxPeriodRepository periodRepository = mock(TaxPeriodRepository.class);
    private final GSTReturnRepository returnRepository = mock(GSTReturnRepository.class);
    private final TdsTransactionRepository transactionRepository = mock(TdsTransactionRepository.class);
    private final EInvoiceRecordRepository eInvoiceRepository = mock(EInvoiceRecordRepository.class);
    private final EWayBillRecordRepository eWayBillRepository = mock(EWayBillRecordRepository.class);
    private final TenantTaxSettingsRepository settingsRepository = mock(TenantTaxSettingsRepository.class);
    private final TaxApiService service = new TaxApiService(
            registrationRepository,
            invoiceRepository,
            noteRepository,
            hsnRepository,
            periodRepository,
            returnRepository,
            transactionRepository,
            eInvoiceRepository,
            eWayBillRepository,
            new GSTCalculationService(),
            new GSTReturnService(invoiceRepository, noteRepository),
            new TDSService(),
            new StaticListableBeanFactory().getBeanProvider(EInvoiceProvider.class),
            new StaticListableBeanFactory().getBeanProvider(EWayBillProvider.class));

    @BeforeEach
    void configureRepositories() {
        TenantTaxSettings settings = new TenantTaxSettings();
        settings.setGstEnabled(true);
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));
        ReflectionTestUtils.setField(service, "tenantTaxSettingsRepository", settingsRepository);
        when(periodRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.findByInvoiceNumber("INV-1")).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(TaxInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsDraftInvoiceWithCalculatedTaxAndSupplierRegistration() {
        GSTRegistration supplier = new GSTRegistration();
        supplier.setGstin("29ABCDE1234F1Z5");
        supplier.setStateCode("29");
        supplier.setRegistrationType(GSTRegistrationType.REGULAR);
        when(registrationRepository.findAllByActiveTrue()).thenReturn(List.of(supplier));

        TaxInvoice invoice = new TaxInvoice();
        invoice.setInvoiceNumber("INV-1");
        invoice.setInvoiceDate(LocalDate.of(2026, 10, 8));
        invoice.setCustomerName("Customer");
        invoice.setCustomerStateCode("29");
        invoice.setLines(List.of(line()));

        TaxInvoice created = service.createInvoice(invoice);

        assertEquals(InvoiceStatus.DRAFT, created.getStatus());
        assertEquals("29ABCDE1234F1Z5", created.getSupplierGSTIN());
        assertEquals(new BigDecimal("100.00"), created.getTaxableAmount());
        assertEquals(new BigDecimal("9.00"), created.getCgst());
        assertEquals(new BigDecimal("9.00"), created.getSgst());
        assertEquals(new BigDecimal("118.00"), created.getGrandTotal());
    }

    @Test
    void requiresSupplierRegistrationWhenThereAreSeveralActiveGstins() {
        when(registrationRepository.findAllByActiveTrue()).thenReturn(List.of(
                registration("29ABCDE1234F1Z5", "29"),
                registration("07ABCDE1234F1Z6", "07")));

        TaxInvoice invoice = new TaxInvoice();
        invoice.setInvoiceNumber("INV-1");
        invoice.setInvoiceDate(LocalDate.of(2026, 10, 8));
        invoice.setCustomerStateCode("29");
        invoice.setLines(List.of(line()));

        assertThrows(ResponseStatusException.class, () -> service.createInvoice(invoice));
    }

    @Test
    void calculatesReturnNoteTaxesFromOriginalInvoiceLines() {
        TaxInvoice original = new TaxInvoice();
        ReflectionTestUtils.setField(original, "id", 91L);
        original.setTransactionType("SALES");
        original.setSourceType("SALE");
        original.setSourceId(27L);
        original.setStatus(InvoiceStatus.CONFIRMED);
        original.setCustomerName("Customer");
        TaxInvoiceLine originalLine = new TaxInvoiceLine();
        ReflectionTestUtils.setField(originalLine, "id", 53L);
        originalLine.setProductId(15L);
        originalLine.setDescription("Widget");
        originalLine.setHsnSacCode("1234");
        originalLine.setQuantity(new BigDecimal("2"));
        originalLine.setRate(new BigDecimal("100"));
        originalLine.setTaxableAmount(new BigDecimal("200"));
        originalLine.setGstRate(new BigDecimal("18"));
        originalLine.setCgst(new BigDecimal("18"));
        originalLine.setSgst(new BigDecimal("18"));
        originalLine.setIgst(BigDecimal.ZERO);
        originalLine.setCess(BigDecimal.ZERO);
        original.setLines(List.of(originalLine));
        when(invoiceRepository.findById(91L)).thenReturn(Optional.of(original));
        when(noteRepository.findByNoteNumber("CN-1")).thenReturn(Optional.empty());
        when(noteRepository.findByOriginalInvoiceId(91L)).thenReturn(List.of());
        when(noteRepository.save(any(CreditDebitNote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreditDebitNote input = new CreditDebitNote();
        input.setNoteNumber("CN-1");
        input.setNoteDate(LocalDate.of(2026, 10, 12));
        input.setNoteType(NoteType.CREDIT);
        input.setTransactionType("SALES");
        input.setOriginalInvoiceId(91L);
        input.setTaxableAmount(new BigDecimal("9999"));
        input.setCgst(new BigDecimal("9999"));
        CreditDebitNoteLine returnedLine = new CreditDebitNoteLine();
        returnedLine.setOriginalInvoiceLineId(53L);
        returnedLine.setQuantity(BigDecimal.ONE);
        input.setLines(List.of(returnedLine));

        CreditDebitNote result = service.createNote(input);

        assertEquals(new BigDecimal("100.00"), result.getTaxableAmount());
        assertEquals(new BigDecimal("9.00"), result.getCgst());
        assertEquals(new BigDecimal("9.00"), result.getSgst());
        assertEquals(new BigDecimal("118.00"), result.getTotal());
        assertEquals(15L, result.getLines().get(0).getProductId());
    }

    private GSTRegistration registration(String gstin, String stateCode) {
        GSTRegistration registration = new GSTRegistration();
        registration.setGstin(gstin);
        registration.setStateCode(stateCode);
        registration.setRegistrationType(GSTRegistrationType.REGULAR);
        return registration;
    }

    private TaxInvoiceLine line() {
        TaxInvoiceLine line = new TaxInvoiceLine();
        line.setDescription("Item");
        line.setQuantity(BigDecimal.ONE);
        line.setRate(new BigDecimal("100"));
        line.setGstRate(new BigDecimal("18"));
        return line;
    }
}
