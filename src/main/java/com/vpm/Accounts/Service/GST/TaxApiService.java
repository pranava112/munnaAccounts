package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.DTO.GST.GSTR1SummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTR3BSummaryDTO;
import com.vpm.Accounts.DTO.GST.GSTCalculationRequest;
import com.vpm.Accounts.DTO.GST.GSTCalculationResponse;
import com.vpm.Accounts.DTO.GST.TaxCalculationRequest;
import com.vpm.Accounts.DTO.GST.TaxReconciliationDTO;
import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Entity.GST.*;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Enum.InvoiceStatus;
import com.vpm.Accounts.Repository.GST.*;
import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Entity.PurchaseItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.Repository.AccountingPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class TaxApiService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final Collection<InvoiceStatus> RETURNABLE_STATUSES = List.of(
            InvoiceStatus.CONFIRMED,
            InvoiceStatus.E_INVOICE_GENERATED,
            InvoiceStatus.E_WAY_BILL_GENERATED);

    private final GSTRegistrationRepository registrationRepository;
    private final TaxInvoiceRepository invoiceRepository;
    private final CreditDebitNoteRepository noteRepository;
    private final HsnSacCodeRepository hsnRepository;
    private final TaxPeriodRepository periodRepository;
    private final GSTReturnRepository returnRepository;
    private final TdsTransactionRepository transactionRepository;
    private final EInvoiceRecordRepository eInvoiceRepository;
    private final EWayBillRecordRepository eWayBillRepository;
    private final GSTCalculationService calculationService;
    private final GSTReturnService returnService;
    private final TDSService tdsService;
    private final ObjectProvider<EInvoiceProvider> eInvoiceProviders;
    private final ObjectProvider<EWayBillProvider> eWayBillProviders;

    @Autowired
    private TenantTaxSettingsRepository tenantTaxSettingsRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private FinancialYearRepository financialYearRepository;

    @Autowired
    private AccountingPeriodRepository accountingPeriodRepository;

    public TaxApiService(
            GSTRegistrationRepository registrationRepository,
            TaxInvoiceRepository invoiceRepository,
            CreditDebitNoteRepository noteRepository,
            HsnSacCodeRepository hsnRepository,
            TaxPeriodRepository periodRepository,
            GSTReturnRepository returnRepository,
            TdsTransactionRepository transactionRepository,
            EInvoiceRecordRepository eInvoiceRepository,
            EWayBillRecordRepository eWayBillRepository,
            GSTCalculationService calculationService,
            GSTReturnService returnService,
            TDSService tdsService,
            ObjectProvider<EInvoiceProvider> eInvoiceProviders,
            ObjectProvider<EWayBillProvider> eWayBillProviders) {
        this.registrationRepository = registrationRepository;
        this.invoiceRepository = invoiceRepository;
        this.noteRepository = noteRepository;
        this.hsnRepository = hsnRepository;
        this.periodRepository = periodRepository;
        this.returnRepository = returnRepository;
        this.transactionRepository = transactionRepository;
        this.eInvoiceRepository = eInvoiceRepository;
        this.eWayBillRepository = eWayBillRepository;
        this.calculationService = calculationService;
        this.returnService = returnService;
        this.tdsService = tdsService;
        this.eInvoiceProviders = eInvoiceProviders;
        this.eWayBillProviders = eWayBillProviders;
    }

    @Transactional(readOnly = true)
    public TenantTaxSettings taxSettings() {
        return tenantTaxSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(TenantTaxSettings::new);
    }

    @Transactional
    public TenantTaxSettings updateTaxSettings(TenantTaxSettings input) {
        if (input == null) {
            throw badRequest("GST settings are required");
        }
        TenantTaxSettings settings = tenantTaxSettingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(TenantTaxSettings::new);
        settings.setGstEnabled(input.isGstEnabled());
        settings.setTaxInclusive(input.isTaxInclusive());
        return tenantTaxSettingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public GSTCalculationResponse calculateGst(GSTCalculationRequest request) {
        requireGstEnabled();
        if (request == null) {
            throw badRequest("GST calculation details are required");
        }
        List<GSTRegistration> registrations = registrationRepository.findAllByActiveTrue();
        if (registrations.size() != 1) {
            throw badRequest("GST calculation requires exactly one active company GST registration");
        }
        GSTRegistration registration = registrations.get(0);
        request.setSupplierStateCode(registration.getStateCode());
        request.setTaxInclusive(taxSettings().isTaxInclusive());
        return calculationService.calculate(request);
    }

    @Transactional
    public void syncSaleTax(Sale sale) {
        if (sale == null || sale.getId() == null) {
            throw badRequest("A saved sale is required for GST processing");
        }
        if (!taxSettings().isGstEnabled()) {
            if (invoiceRepository.findBySourceTypeAndSourceId("SALE", sale.getId()).isPresent()) {
                throw conflict("This sale already has GST history. Re-enable GST before changing it.");
            }
            resetSaleTax(sale);
            return;
        }
        Account customer = accountRepository.findFirstByNameIgnoreCase(sale.getCustomerName())
                .orElseThrow(() -> badRequest("GST is enabled: create the customer account with a state before posting this sale"));
        List<TaxInvoiceLine> lines = new ArrayList<>();
        for (SaleItem item : sale.getItems()) {
            lines.add(sourceLine(item.getProduct(), item.getQuantity(), item.getPrice()));
        }
        TaxInvoice invoice = saveSourceInvoice(
                "SALE", sale.getId(), sale.getVoucherNumber(), sale.getDate(),
                sale.getCustomerName(), customer.getGstin(), customer.getStateCode(),
                lines, sale.getSubtotal(), sale.getDiscountAmount());
        sale.setGstTaxableAmount(invoice.getTaxableAmount());
        sale.setGstCgst(invoice.getCgst());
        sale.setGstSgst(invoice.getSgst());
        sale.setGstIgst(invoice.getIgst());
        sale.setGstCess(invoice.getCess());
        sale.setGstTotal(invoice.getTotalTax());
        sale.setGrandTotal(invoice.getGrandTotal());
        BigDecimal due = invoice.getGrandTotal().subtract(safe(sale.getCommissionAmount())).max(BigDecimal.ZERO);
        BigDecimal cashPaid = sale.isPartialCashEnabled() ? safe(sale.getCashPaid()) : BigDecimal.ZERO;
        if (cashPaid.compareTo(due) > 0) {
            throw badRequest("Cash paid cannot exceed the GST-inclusive amount due");
        }
        sale.setCreditAmount(due.subtract(cashPaid));
    }

    @Transactional
    public void syncPurchaseTax(Purchase purchase) {
        if (purchase == null || purchase.getId() == null) {
            throw badRequest("A saved purchase is required for GST processing");
        }
        if (!taxSettings().isGstEnabled()) {
            if (invoiceRepository.findBySourceTypeAndSourceId("PURCHASE", purchase.getId()).isPresent()) {
                throw conflict("This purchase already has GST history. Re-enable GST before changing it.");
            }
            resetPurchaseTax(purchase);
            return;
        }
        Account supplier = accountRepository.findFirstByNameIgnoreCase(purchase.getSupplierName())
                .orElseThrow(() -> badRequest("GST is enabled: create the supplier account with a state before posting this purchase"));
        List<TaxInvoiceLine> lines = new ArrayList<>();
        for (PurchaseItem item : purchase.getItems()) {
            lines.add(sourceLine(item.getProduct(), item.getQuantity(), item.getPrice()));
        }
        TaxInvoice invoice = saveSourceInvoice(
                "PURCHASE", purchase.getId(), purchase.getVoucherNumber(), purchase.getDate(),
                purchase.getSupplierName(), supplier.getGstin(), supplier.getStateCode(),
                lines, purchase.getItems().stream()
                        .map(PurchaseItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add),
                BigDecimal.ZERO);
        purchase.setGstTaxableAmount(invoice.getTaxableAmount());
        purchase.setGstCgst(invoice.getCgst());
        purchase.setGstSgst(invoice.getSgst());
        purchase.setGstIgst(invoice.getIgst());
        purchase.setGstCess(invoice.getCess());
        purchase.setGstTotal(invoice.getTotalTax());
        purchase.setGrandTotal(invoice.getGrandTotal());
    }

    private TaxInvoiceLine sourceLine(Product product, int quantity, BigDecimal rate) {
        if (product == null) {
            throw badRequest("Every GST transaction line must reference a product");
        }
        TaxInvoiceLine line = new TaxInvoiceLine();
        line.setDescription(product.getName());
        line.setHsnSacCode(product.getHsnSacCode());
        line.setProductId(product.getId());
        line.setQuantity(BigDecimal.valueOf(quantity));
        line.setRate(rate);
        line.setGstRate(safe(product.getGstRate()));
        line.setCessRate(safe(product.getCessRate()));
        return line;
    }

    private TaxInvoice saveSourceInvoice(
            String sourceType, Long sourceId, String invoiceNumber, LocalDate date,
            String partyName, String partyGstin, String partyStateCode,
            List<TaxInvoiceLine> lines, BigDecimal subtotal, BigDecimal discount) {
        if (date == null || invoiceNumber == null || invoiceNumber.isBlank()) {
            throw badRequest("GST transaction date and voucher number are required");
        }
        requireGstEnabled();
        TenantTaxSettings settings = tenantTaxSettingsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> badRequest("GST settings must be saved before posting a GST transaction"));
        List<GSTRegistration> registrations = registrationRepository.findAllByActiveTrue();
        if (registrations.size() != 1) {
            throw badRequest("GST-enabled posting requires exactly one active company GST registration");
        }
        GSTRegistration company = registrations.get(0);
        String companyState = requiredSupplierStateCode(company);
        if (partyStateCode == null || !partyStateCode.trim().matches("\\d{2}")) {
            throw badRequest("GST-enabled posting requires a two-digit state code on the customer or supplier account");
        }
        String normalizedPartyGstin = normalizeOptionalGstin(partyGstin);
        if (normalizedPartyGstin != null
                && !normalizedPartyGstin.substring(0, 2).equals(partyStateCode.trim())) {
            throw badRequest("Customer or supplier GSTIN state code must match the account state");
        }
        validatePeriodOpen(date);
        boolean purchase = "PURCHASE".equals(sourceType);
        String supplierState = purchase ? partyStateCode.trim() : companyState;
        String customerState = purchase ? companyState : partyStateCode.trim();
        TaxInvoice invoice = invoiceRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .orElseGet(TaxInvoice::new);
        if (invoice.getId() != null && invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw conflict("A cancelled GST invoice cannot be reactivated by editing its source voucher");
        }
        if (invoice.getId() != null && hasActiveNotes(invoice.getId())) {
            throw conflict("A GST invoice with credit/debit notes cannot be changed; create an adjustment note instead");
        }
        if (invoice.getId() == null) {
            invoice.setInvoiceNumber(invoiceNumber);
        }
        invoice.setInvoiceDate(date);
        invoice.setCustomerName(partyName);
        invoice.setTransactionType(purchase ? "PURCHASE" : "SALES");
        invoice.setSourceType(sourceType);
        invoice.setSourceId(sourceId);
        invoice.setSupplierGSTIN(purchase ? normalizedPartyGstin : company.getGstin());
        invoice.setCustomerGSTIN(purchase ? company.getGstin() : normalizedPartyGstin);
        invoice.setCustomerStateCode(customerState);
        invoice.setPlaceOfSupply(partyStateCode.trim());

        BigDecimal totalBeforeDiscount = safe(subtotal);
        BigDecimal discountFactor = totalBeforeDiscount.signum() > 0
                ? totalBeforeDiscount.subtract(safe(discount)).max(BigDecimal.ZERO)
                        .divide(totalBeforeDiscount, 10, RoundingMode.HALF_UP)
                : BigDecimal.ONE;
        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        BigDecimal cess = BigDecimal.ZERO;
        List<TaxInvoiceLine> calculatedLines = new ArrayList<>();
        for (TaxInvoiceLine source : lines) {
            if (source.getQuantity() == null || source.getQuantity().signum() <= 0
                    || source.getRate() == null || source.getRate().signum() < 0) {
                throw badRequest("GST product line quantity and price are invalid");
            }
            if (source.getHsnSacCode() == null || !source.getHsnSacCode().trim().matches("\\d{4,8}")) {
                throw badRequest("GST-enabled products must have a valid 4 to 8 digit HSN/SAC code");
            }
            BigDecimal adjustedRate = source.getRate().multiply(discountFactor)
                    .setScale(2, RoundingMode.HALF_UP);
            GSTCalculationRequest request = new GSTCalculationRequest();
            request.setTaxableAmount(adjustedRate.multiply(source.getQuantity()));
            request.setGstRate(safe(source.getGstRate()));
            request.setCessRate(safe(source.getCessRate()));
            request.setTaxInclusive(settings.isTaxInclusive());
            request.setSupplierStateCode(supplierState);
            request.setCustomerStateCode(customerState);
            var result = calculationService.calculate(request);
            source.setRate(adjustedRate);
            source.setInvoice(invoice);
            source.setTaxInclusive(settings.isTaxInclusive());
            source.setTaxableAmount(result.getTaxableAmount());
            source.setCgst(result.getCgst());
            source.setSgst(result.getSgst());
            source.setIgst(result.getIgst());
            source.setCess(result.getCess());
            calculatedLines.add(source);
            taxable = taxable.add(result.getTaxableAmount());
            cgst = cgst.add(result.getCgst());
            sgst = sgst.add(result.getSgst());
            igst = igst.add(result.getIgst());
            cess = cess.add(result.getCess());
        }
        invoice.setLines(calculatedLines);
        invoice.setTaxableAmount(taxable);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(igst);
        invoice.setCess(cess);
        invoice.setTotalTax(cgst.add(sgst).add(igst).add(cess));
        invoice.setGrandTotal(invoice.getTaxableAmount().add(invoice.getTotalTax()));
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        return invoiceRepository.save(invoice);
    }

    private String normalizeOptionalGstin(String gstin) {
        if (gstin == null || gstin.isBlank()) return null;
        String normalized = gstin.trim().toUpperCase();
        if (!isValidGstin(normalized)) throw badRequest("Customer or supplier GSTIN is invalid");
        return normalized;
    }

    @Transactional
    public void cancelSourceInvoice(String sourceType, Long sourceId) {
        invoiceRepository.findBySourceTypeAndSourceId(sourceType, sourceId).ifPresent(invoice -> {
            if (hasActiveNotes(invoice.getId())) {
                throw conflict("GST invoice with adjustment notes cannot be removed");
            }
            invoice.setStatus(InvoiceStatus.CANCELLED);
            invoiceRepository.save(invoice);
        });
    }

    private void resetSaleTax(Sale sale) {
        sale.setGstTaxableAmount(BigDecimal.ZERO);
        sale.setGstCgst(BigDecimal.ZERO);
        sale.setGstSgst(BigDecimal.ZERO);
        sale.setGstIgst(BigDecimal.ZERO);
        sale.setGstCess(BigDecimal.ZERO);
        sale.setGstTotal(BigDecimal.ZERO);
        sale.setGrandTotal(safe(sale.getNetAmount()));
        sale.setCreditAmount(safe(sale.getNetAmount()).subtract(safe(sale.getCashPaid()))
                .subtract(safe(sale.getCommissionAmount())).max(BigDecimal.ZERO));
    }

    private void resetPurchaseTax(Purchase purchase) {
        purchase.setGstTaxableAmount(BigDecimal.ZERO);
        purchase.setGstCgst(BigDecimal.ZERO);
        purchase.setGstSgst(BigDecimal.ZERO);
        purchase.setGstIgst(BigDecimal.ZERO);
        purchase.setGstCess(BigDecimal.ZERO);
        purchase.setGstTotal(BigDecimal.ZERO);
        purchase.setGrandTotal(purchase.getItems() == null ? BigDecimal.ZERO
                : purchase.getItems().stream().map(PurchaseItem::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Transactional(readOnly = true)
    public List<GSTRegistration> registrations() {
        return registrationRepository.findAll();
    }

    @Transactional
    public GSTRegistration createRegistration(GSTRegistration input) {
        validateRegistration(input);
        String gstin = input.getGstin().trim().toUpperCase();
        if (registrationRepository.existsByGstin(gstin)) {
            throw conflict("GSTIN is already registered");
        }
        GSTRegistration registration = new GSTRegistration();
        copyRegistration(input, registration);
        registration.setGstin(gstin);
        return registrationRepository.save(registration);
    }

    @Transactional
    public GSTRegistration updateRegistration(Long id, GSTRegistration input) {
        validateRegistration(input);
        GSTRegistration existing = registrationRepository.findById(id)
                .orElseThrow(() -> notFound("GST registration not found"));
        String gstin = input.getGstin().trim().toUpperCase();
        registrationRepository.findByGstin(gstin).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw conflict("GSTIN is already registered");
            }
        });
        existing.setGstin(gstin);
        existing.setLegalName(input.getLegalName().trim());
        existing.setTradeName(input.getTradeName());
        existing.setState(input.getState());
        existing.setStateCode(input.getStateCode());
        existing.setAddress(input.getAddress());
        existing.setRegistrationDate(input.getRegistrationDate());
        existing.setActive(input.isActive());
        existing.setRegistrationType(input.getRegistrationType());
        existing.setEInvoiceEnabled(input.isEInvoiceEnabled());
        existing.setEWayBillEnabled(input.isEWayBillEnabled());
        return registrationRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<HsnSacCode> hsnSacCodes() {
        return hsnRepository.findAllByActiveTrueOrderByCodeAsc();
    }

    @Transactional(readOnly = true)
    public HsnSacCode hsnSacCode(String code) {
        return hsnRepository.findByCodeAndActiveTrue(code.trim())
                .orElseThrow(() -> notFound("HSN/SAC code not found"));
    }

    @Transactional
    public HsnSacCode createHsnSacCode(HsnSacCode input) {
        if (input == null || input.getCode() == null || input.getCode().isBlank()
                || input.getType() == null) {
            throw badRequest("Code and HSN/SAC type are required");
        }
        String code = input.getCode().trim();
        if (!code.matches("\\d{4,8}")) {
            throw badRequest("HSN/SAC code must contain 4 to 8 digits");
        }
        if (hsnRepository.existsByCode(code)) {
            throw conflict("HSN/SAC code already exists");
        }
        input.setCode(code);
        input.setActive(true);
        return hsnRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<TaxInvoice> invoices() {
        requireGstEnabled();
        return invoiceRepository.findAllByOrderByInvoiceDateDesc();
    }

    @Transactional(readOnly = true)
    public TaxInvoice invoice(Long id) {
        requireGstEnabled();
        return findInvoice(id);
    }

    @Transactional
    public TaxInvoice createInvoice(TaxInvoice input) {
        requireGstEnabled();
        validateInvoiceInput(input);
        String number = input.getInvoiceNumber().trim();
        if (invoiceRepository.findByInvoiceNumber(number).isPresent()) {
            throw conflict("Invoice number already exists");
        }
        TaxInvoice invoice = new TaxInvoice();
        copyInvoice(input, invoice);
        invoice.setInvoiceNumber(number);
        invoice.setStatus(InvoiceStatus.DRAFT);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public TaxInvoice updateInvoice(Long id, TaxInvoice input) {
        requireGstEnabled();
        validateInvoiceInput(input);
        TaxInvoice existing = findInvoice(id);
        if (existing.getStatus() != InvoiceStatus.DRAFT) {
            throw conflict("Only draft invoices can be edited");
        }
        String number = input.getInvoiceNumber().trim();
        invoiceRepository.findByInvoiceNumber(number).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw conflict("Invoice number already exists");
            }
        });
        copyInvoice(input, existing);
        existing.setInvoiceNumber(number);
        return invoiceRepository.save(existing);
    }

    @Transactional
    public TaxInvoice confirmInvoice(Long id) {
        requireGstEnabled();
        TaxInvoice invoice = findInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw conflict("Only draft invoices can be confirmed");
        }
        validatePeriodOpen(invoice.getInvoiceDate());
        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            throw badRequest("Invoice must contain at least one line");
        }
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public TaxInvoice cancelInvoice(Long id) {
        requireGstEnabled();
        TaxInvoice invoice = findInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw conflict("Invoice is already cancelled");
        }
        validatePeriodOpen(invoice.getInvoiceDate());
        if (hasLiveEInvoice(id) || hasLiveEWayBill(id)) {
            throw conflict("Cancel the active e-invoice and e-way bill with the GST gateway before cancelling this invoice");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public List<CreditDebitNote> notes() {
        requireGstEnabled();
        return noteRepository.findAllWithLines();
    }

    @Transactional(readOnly = true)
    public CreditDebitNote note(Long id) {
        requireGstEnabled();
        return noteRepository.findById(id)
                .orElseThrow(() -> notFound("Credit/debit note not found"));
    }

    @Transactional
    public CreditDebitNote createNote(CreditDebitNote input) {
        requireGstEnabled();
        validateNoteInput(input);
        CreditDebitNote note = new CreditDebitNote();
        note.setNoteNumber(input.getNoteNumber().trim());
        note.setNoteDate(input.getNoteDate());
        note.setNoteType(input.getNoteType());
        note.setOriginalInvoiceId(input.getOriginalInvoiceId());
        note.setTransactionType(input.getTransactionType());
        note.setReason(input.getReason());
        note.setTaxableAmount(input.getTaxableAmount());
        note.setCgst(input.getCgst());
        note.setSgst(input.getSgst());
        note.setIgst(input.getIgst());
        note.setCess(input.getCess());
        note.setTotal(input.getTotal());
        List<CreditDebitNoteLine> noteLines = input.getLines().stream().map(source -> {
            CreditDebitNoteLine line = new CreditDebitNoteLine();
            line.setOriginalInvoiceLineId(source.getOriginalInvoiceLineId());
            line.setProductId(source.getProductId());
            line.setDescription(source.getDescription());
            line.setHsnSacCode(source.getHsnSacCode());
            line.setQuantity(source.getQuantity());
            line.setRate(source.getRate());
            line.setTaxableAmount(source.getTaxableAmount());
            line.setGstRate(source.getGstRate());
            line.setCessRate(source.getCessRate());
            line.setCgst(source.getCgst());
            line.setSgst(source.getSgst());
            line.setIgst(source.getIgst());
            line.setCess(source.getCess());
            return line;
        }).toList();
        note.setLines(noteLines);
        note.setStatus(InvoiceStatus.DRAFT);
        return noteRepository.save(note);
    }

    @Transactional
    public CreditDebitNote confirmNote(Long id) {
        requireGstEnabled();
        CreditDebitNote note = note(id);
        if (note.getStatus() != InvoiceStatus.DRAFT) {
            throw conflict("Only draft notes can be confirmed");
        }
        validatePeriodOpen(note.getNoteDate());
        applyReturnEffects(note);
        note.setStatus(InvoiceStatus.CONFIRMED);
        return noteRepository.save(note);
    }

    @Transactional
    public CreditDebitNote cancelNote(Long id) {
        requireGstEnabled();
        CreditDebitNote note = note(id);
        if (note.getStatus() != InvoiceStatus.DRAFT) {
            throw conflict("Only draft notes can be cancelled");
        }
        note.setStatus(InvoiceStatus.CANCELLED);
        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public GSTR1SummaryDTO gstr1(LocalDate from, LocalDate to) {
        requireGstEnabled();
        validateRange(from, to);
        return returnService.generateGSTR1(from, to);
    }

    @Transactional(readOnly = true)
    public GSTR3BSummaryDTO gstr3b(int year, int month) {
        requireGstEnabled();
        YearMonth reportingMonth = yearMonth(year, month);
        LocalDate from = reportingMonth.atDay(1);
        LocalDate to = reportingMonth.atEndOfMonth();
        List<TaxInvoice> confirmedSales =
                invoiceRepository.findByTransactionTypeAndInvoiceDateBetweenAndStatusIn(
                        "SALES", from, to, RETURNABLE_STATUSES);
        List<TaxInvoice> confirmedPurchases =
                invoiceRepository.findByTransactionTypeAndInvoiceDateBetweenAndStatusIn(
                        "PURCHASE", from, to, RETURNABLE_STATUSES);
        GSTR3BSummaryDTO summary = new GSTR3BSummaryDTO();
        summary.setYear(year);
        summary.setMonth(month);
        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        BigDecimal cess = BigDecimal.ZERO;
        BigDecimal inputCgst = BigDecimal.ZERO;
        BigDecimal inputSgst = BigDecimal.ZERO;
        BigDecimal inputIgst = BigDecimal.ZERO;
        BigDecimal inputCess = BigDecimal.ZERO;
        for (TaxInvoice invoice : confirmedSales) {
            taxable = taxable.add(safe(invoice.getTaxableAmount()));
            cgst = cgst.add(safe(invoice.getCgst()));
            sgst = sgst.add(safe(invoice.getSgst()));
            igst = igst.add(safe(invoice.getIgst()));
            cess = cess.add(safe(invoice.getCess()));
        }
        for (TaxInvoice invoice : confirmedPurchases) {
            inputCgst = inputCgst.add(safe(invoice.getCgst()));
            inputSgst = inputSgst.add(safe(invoice.getSgst()));
            inputIgst = inputIgst.add(safe(invoice.getIgst()));
            inputCess = inputCess.add(safe(invoice.getCess()));
        }
        List<CreditDebitNote> notes = noteRepository.findByNoteDateBetweenAndStatus(
                from, to, InvoiceStatus.CONFIRMED);
        for (CreditDebitNote note : notes) {
            BigDecimal adjustment = isCreditNote(note) ? BigDecimal.valueOf(-1) : BigDecimal.ONE;
            if ("PURCHASE".equalsIgnoreCase(note.getTransactionType())) {
                inputCgst = inputCgst.subtract(safe(note.getCgst()).multiply(adjustment));
                inputSgst = inputSgst.subtract(safe(note.getSgst()).multiply(adjustment));
                inputIgst = inputIgst.subtract(safe(note.getIgst()).multiply(adjustment));
                inputCess = inputCess.subtract(safe(note.getCess()).multiply(adjustment));
            } else {
                taxable = taxable.add(safe(note.getTaxableAmount()).multiply(adjustment));
                cgst = cgst.add(safe(note.getCgst()).multiply(adjustment));
                sgst = sgst.add(safe(note.getSgst()).multiply(adjustment));
                igst = igst.add(safe(note.getIgst()).multiply(adjustment));
                cess = cess.add(safe(note.getCess()).multiply(adjustment));
            }
        }
        summary.setOutwardTaxable(taxable);
        summary.setCgst(cgst);
        summary.setSgst(sgst);
        summary.setIgst(igst);
        summary.setCess(cess);
        summary.setTotalTax(cgst.add(sgst).add(igst).add(cess));
        summary.setInputCgst(inputCgst);
        summary.setInputSgst(inputSgst);
        summary.setInputIgst(inputIgst);
        summary.setInputCess(inputCess);
        summary.setTaxPayable(summary.getTotalTax()
                .subtract(inputCgst.add(inputSgst).add(inputIgst).add(inputCess))
                .max(BigDecimal.ZERO));
        summary.setStatus("ESTIMATED");
        int financialYearStart = month >= 4 ? year : year - 1;
        String financialYear = financialYearStart + "-"
                + String.format("%02d", (financialYearStart + 1) % 100);
        List<GSTReturn> declaredReturns =
                returnRepository.findAllByFinancialYearAndMonth(financialYear, month);
        if (!declaredReturns.isEmpty()) {
            BigDecimal itcReversal = BigDecimal.ZERO;
            BigDecimal reverseCharge = BigDecimal.ZERO;
            BigDecimal taxPayable = BigDecimal.ZERO;
            BigDecimal taxPaid = BigDecimal.ZERO;
            for (GSTReturn declared : declaredReturns) {
                itcReversal = itcReversal.add(safe(declared.getItcReversal()));
                reverseCharge = reverseCharge.add(safe(declared.getReverseCharge()));
                taxPayable = taxPayable.add(safe(declared.getTaxPayable()));
                taxPaid = taxPaid.add(safe(declared.getTaxPaid()));
            }
            summary.setItcReversal(itcReversal);
            summary.setReverseCharge(reverseCharge);
            summary.setTaxPayable(taxPayable);
            summary.setTaxPaid(taxPaid);
            summary.setStatus(declaredReturns.size() == 1 ? declaredReturns.get(0).getStatus() : "AGGREGATED");
        }
        return summary;
    }

    @Transactional(readOnly = true)
    public List<TaxInvoice> salesRegister(LocalDate from, LocalDate to) {
        requireGstEnabled();
        validateRange(from, to);
        return invoiceRepository.findByTransactionTypeAndInvoiceDateBetweenAndStatusIn(
                "SALES", from, to, RETURNABLE_STATUSES);
    }

    @Transactional
    public TdsTransaction calculateWithholding(String type, TaxCalculationRequest request) {
        if (request == null || request.getTransactionDate() == null) {
            throw badRequest("Transaction date is required");
        }
        BigDecimal amount = request.getAmount();
        BigDecimal rate = request.getRate();
        if (amount == null || amount.signum() < 0) {
            throw badRequest("Amount must be zero or greater");
        }
        if (rate == null || rate.signum() < 0 || rate.compareTo(HUNDRED) > 0) {
            throw badRequest("Rate must be between 0 and 100");
        }
        validatePeriodOpen(request.getTransactionDate());
        TdsTransaction transaction = new TdsTransaction();
        transaction.setType(type);
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setReference(request.getReference());
        transaction.setPartyName(request.getPartyName());
        BigDecimal roundedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        transaction.setTaxableAmount(roundedAmount);
        transaction.setRate(rate);
        transaction.setTaxAmount(tdsService.calculate(roundedAmount, rate));
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TdsTransaction> withholdingTransactions(String type) {
        return transactionRepository.findByTypeOrderByTransactionDateDesc(type);
    }

    @Transactional(readOnly = true)
    public List<TdsTransaction> withholdingReport(String type, LocalDate from, LocalDate to) {
        validateRange(from, to);
        return transactionRepository.findByTypeAndTransactionDateBetweenOrderByTransactionDateDesc(
                type, from, to);
    }

    @Transactional(readOnly = true)
    public List<TaxPeriod> periods() {
        requireGstEnabled();
        return periodRepository.findAllByOrderByStartDateDesc();
    }

    @Transactional
    public TaxPeriod setPeriodLocked(Long id, boolean locked) {
        requireGstEnabled();
        TaxPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> notFound("Tax period not found"));
        period.setLocked(locked);
        period.setLockedAt(locked ? LocalDate.now() : null);
        period.setLockedBy(locked
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null);
        return periodRepository.save(period);
    }

    @Transactional(readOnly = true)
    public TaxReconciliationDTO reconciliation(Long periodId) {
        requireGstEnabled();
        return buildReconciliation(findPeriod(periodId));
    }

    @Transactional
    public TaxReconciliationDTO reconcile(Long periodId) {
        requireGstEnabled();
        TaxPeriod period = findPeriod(periodId);
        TaxReconciliationDTO result = buildReconciliation(period);
        boolean matched = result.getTaxableDifference().signum() == 0
                && result.getTaxDifference().signum() == 0;
        period.setReconciliationStatus(matched ? "RECONCILED" : "MISMATCH");
        periodRepository.save(period);
        result.setStatus(period.getReconciliationStatus());
        return result;
    }

    @Transactional
    public EInvoiceRecord generateEInvoice(Long invoiceId) {
        requireGstEnabled();
        TaxInvoice invoice = findInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.CONFIRMED) {
            throw conflict("E-invoices can only be generated for confirmed invoices");
        }
        EInvoiceProvider provider = eInvoiceProviders.getIfAvailable();
        if (provider == null) {
            throw unavailable("No EInvoiceProvider is configured. Connect a GST gateway adapter before generating e-invoices.");
        }
        if (eInvoiceRepository.findByInvoiceId(invoiceId).map(EInvoiceRecord::getIrn)
                .filter(irn -> !irn.isBlank()).isPresent()) {
            throw conflict("An e-invoice already exists for this invoice");
        }
        var response = provider.generate(invoice);
        if (response == null || response.getIrn() == null || response.getIrn().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GST gateway did not return a valid IRN");
        }
        requireGatewaySuccess(response.getStatus(), "e-invoice generation");
        EInvoiceRecord record = eInvoiceRepository.findByInvoiceId(invoiceId)
                .orElseGet(EInvoiceRecord::new);
        record.setInvoiceId(invoiceId);
        record.setIrn(response.getIrn());
        record.setAckNumber(response.getAckNo());
        record.setStatus("GENERATED");
        record.setErrorMessage(response.getMessage());
        invoice.setStatus(InvoiceStatus.E_INVOICE_GENERATED);
        invoiceRepository.save(invoice);
        return eInvoiceRepository.save(record);
    }

    @Transactional
    public EInvoiceRecord cancelEInvoice(Long invoiceId, String reason) {
        requireGstEnabled();
        if (reason == null || reason.isBlank()) {
            throw badRequest("Cancellation reason is required");
        }
        EInvoiceRecord record = eInvoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> notFound("E-invoice record not found"));
        TaxInvoice invoice = findInvoice(invoiceId);
        validatePeriodOpen(invoice.getInvoiceDate());
        if (hasLiveEWayBill(invoiceId)) {
            throw conflict("Cancel the active e-way bill before cancelling the e-invoice");
        }
        if (record.getIrn() == null || record.getIrn().isBlank()) {
            throw conflict("E-invoice record has no IRN to cancel");
        }
        EInvoiceProvider provider = eInvoiceProviders.getIfAvailable();
        if (provider == null) {
            throw unavailable("No EInvoiceProvider is configured. Connect a GST gateway adapter before cancelling e-invoices.");
        }
        var response = provider.cancel(record.getIrn(), reason.trim());
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GST gateway returned an empty e-invoice cancellation response");
        }
        requireGatewaySuccess(response.getStatus(), "e-invoice cancellation");
        record.setStatus("CANCELLED");
        record.setErrorMessage(response.getMessage());
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        invoiceRepository.save(invoice);
        return eInvoiceRepository.save(record);
    }

    @Transactional(readOnly = true)
    public EInvoiceRecord eInvoice(Long invoiceId) {
        requireGstEnabled();
        return eInvoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> notFound("E-invoice record not found"));
    }

    @Transactional
    public EWayBillRecord generateEWayBill(Long invoiceId) {
        requireGstEnabled();
        TaxInvoice invoice = findInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.CONFIRMED
                && invoice.getStatus() != InvoiceStatus.E_INVOICE_GENERATED) {
            throw conflict("E-way bills can only be generated for confirmed invoices");
        }
        EWayBillProvider provider = eWayBillProviders.getIfAvailable();
        if (provider == null) {
            throw unavailable("No EWayBillProvider is configured. Connect a GST gateway adapter before generating e-way bills.");
        }
        if (eWayBillRepository.findByInvoiceId(invoiceId).map(EWayBillRecord::getEwbNumber)
                .filter(number -> !number.isBlank()).isPresent()) {
            throw conflict("An e-way bill already exists for this invoice");
        }
        var response = provider.generate(invoice);
        if (response == null || response.getEwayBillNo() == null || response.getEwayBillNo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GST gateway did not return a valid e-way bill number");
        }
        requireGatewaySuccess(response.getStatus(), "e-way bill generation");
        EWayBillRecord record = eWayBillRepository.findByInvoiceId(invoiceId)
                .orElseGet(EWayBillRecord::new);
        record.setInvoiceId(invoiceId);
        record.setEwbNumber(response.getEwayBillNo());
        record.setStatus("GENERATED");
        record.setErrorMessage(response.getMessage());
        invoice.setStatus(InvoiceStatus.E_WAY_BILL_GENERATED);
        invoiceRepository.save(invoice);
        return eWayBillRepository.save(record);
    }

    @Transactional
    public EWayBillRecord updateEWayBillVehicle(Long id, String vehicleNumber) {
        requireGstEnabled();
        if (vehicleNumber == null || vehicleNumber.isBlank()) {
            throw badRequest("Vehicle number is required");
        }
        EWayBillRecord record = findEWayBill(id);
        if (record.getEwbNumber() == null || record.getEwbNumber().isBlank()) {
            throw conflict("E-way bill record has no bill number to update");
        }
        EWayBillProvider provider = eWayBillProviders.getIfAvailable();
        if (provider == null) {
            throw unavailable("No EWayBillProvider is configured. Connect a GST gateway adapter before updating vehicles.");
        }
        TaxInvoice invoice = findInvoice(record.getInvoiceId());
        validatePeriodOpen(invoice.getInvoiceDate());
        var response = provider.updateVehicle(record.getEwbNumber(), vehicleNumber.trim());
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GST gateway returned an empty vehicle update response");
        }
        requireGatewaySuccess(response.getStatus(), "vehicle update");
        record.setVehicleNumber(vehicleNumber.trim());
        record.setStatus("UPDATED");
        record.setErrorMessage(response.getMessage());
        return eWayBillRepository.save(record);
    }

    @Transactional
    public EWayBillRecord cancelEWayBill(Long id, String reason) {
        requireGstEnabled();
        if (reason == null || reason.isBlank()) {
            throw badRequest("Cancellation reason is required");
        }
        EWayBillRecord record = findEWayBill(id);
        if (record.getEwbNumber() == null || record.getEwbNumber().isBlank()) {
            throw conflict("E-way bill record has no bill number to cancel");
        }
        EWayBillProvider provider = eWayBillProviders.getIfAvailable();
        if (provider == null) {
            throw unavailable("No EWayBillProvider is configured. Connect a GST gateway adapter before cancelling e-way bills.");
        }
        TaxInvoice invoice = findInvoice(record.getInvoiceId());
        validatePeriodOpen(invoice.getInvoiceDate());
        var response = provider.cancel(record.getEwbNumber(), reason.trim());
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GST gateway returned an empty e-way bill cancellation response");
        }
        requireGatewaySuccess(response.getStatus(), "e-way bill cancellation");
        record.setStatus("CANCELLED");
        record.setErrorMessage(response.getMessage());
        if (hasLiveEInvoice(record.getInvoiceId())) {
            invoice.setStatus(InvoiceStatus.E_INVOICE_GENERATED);
        } else {
            invoice.setStatus(InvoiceStatus.CONFIRMED);
        }
        invoiceRepository.save(invoice);
        return eWayBillRepository.save(record);
    }

    @Transactional(readOnly = true)
    public EWayBillRecord eWayBill(Long id) {
        requireGstEnabled();
        return findEWayBill(id);
    }

    private TaxInvoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> notFound("Tax invoice not found"));
    }

    private EWayBillRecord findEWayBill(Long id) {
        return eWayBillRepository.findById(id)
                .orElseThrow(() -> notFound("E-way bill record not found"));
    }

    private boolean hasLiveEInvoice(Long invoiceId) {
        return eInvoiceRepository.findByInvoiceId(invoiceId)
                .filter(record -> record.getIrn() != null && !record.getIrn().isBlank())
                .filter(record -> !"CANCELLED".equalsIgnoreCase(record.getStatus()))
                .isPresent();
    }

    private boolean hasLiveEWayBill(Long invoiceId) {
        return eWayBillRepository.findByInvoiceId(invoiceId)
                .filter(record -> record.getEwbNumber() != null && !record.getEwbNumber().isBlank())
                .filter(record -> !"CANCELLED".equalsIgnoreCase(record.getStatus()))
                .isPresent();
    }

    private TaxPeriod findPeriod(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> notFound("Tax period not found"));
    }

    private void validateRegistration(GSTRegistration registration) {
        if (registration == null || !isValidGstin(registration.getGstin())
                || registration.getLegalName() == null || registration.getLegalName().isBlank()
                || registration.getRegistrationType() == null
                || registration.getStateCode() == null
                || !registration.getStateCode().matches("\\d{2}")
                || !registration.getGstin().trim().substring(0, 2)
                .equals(registration.getStateCode())) {
            throw badRequest("A valid GSTIN, matching two-digit state code, legal name, and registration type are required");
        }
    }

    private boolean isValidGstin(String gstin) {
        return gstin != null
                && gstin.trim().toUpperCase().matches("\\d{2}[A-Z]{5}\\d{4}[A-Z][1-9A-Z]Z[0-9A-Z]");
    }

    private void copyRegistration(GSTRegistration input, GSTRegistration target) {
        target.setLegalName(input.getLegalName().trim());
        target.setTradeName(input.getTradeName());
        target.setState(input.getState());
        target.setStateCode(input.getStateCode());
        target.setAddress(input.getAddress());
        target.setRegistrationDate(input.getRegistrationDate());
        target.setActive(input.isActive());
        target.setRegistrationType(input.getRegistrationType());
        target.setEInvoiceEnabled(input.isEInvoiceEnabled());
        target.setEWayBillEnabled(input.isEWayBillEnabled());
    }

    private void validateInvoiceInput(TaxInvoice invoice) {
        if (invoice == null || invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()
                || invoice.getInvoiceDate() == null || invoice.getLines() == null || invoice.getLines().isEmpty()) {
            throw badRequest("Invoice number, date, and at least one invoice line are required");
        }
        if (invoice.getCustomerGSTIN() != null && !invoice.getCustomerGSTIN().isBlank()
                && !isValidGstin(invoice.getCustomerGSTIN())) {
            throw badRequest("Customer GSTIN is invalid");
        }
        validatePeriodOpen(invoice.getInvoiceDate());
    }

    private void copyInvoice(TaxInvoice input, TaxInvoice target) {
        target.setInvoiceDate(input.getInvoiceDate());
        target.setCustomerName(input.getCustomerName());
        target.setSupplierGSTIN(input.getSupplierGSTIN());
        target.setCustomerGSTIN(input.getCustomerGSTIN());
        target.setCustomerStateCode(input.getCustomerStateCode());
        target.setPlaceOfSupply(input.getPlaceOfSupply());
        target.setTransactionType(input.getTransactionType() == null
                ? "SALES" : input.getTransactionType().trim().toUpperCase());
        List<TaxInvoiceLine> lines = new ArrayList<>();
        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        BigDecimal cess = BigDecimal.ZERO;
        GSTRegistration supplier;
        if (input.getSupplierGSTIN() == null || input.getSupplierGSTIN().isBlank()) {
            List<GSTRegistration> activeRegistrations = registrationRepository.findAllByActiveTrue();
            if (activeRegistrations.size() != 1) {
                throw badRequest("Provide supplierGSTIN when the tenant has zero or multiple active GST registrations");
            }
            supplier = activeRegistrations.get(0);
        } else {
            supplier = registrationRepository.findByGstinAndActiveTrue(
                            input.getSupplierGSTIN().trim().toUpperCase())
                    .orElseThrow(() -> badRequest("The invoice supplier GSTIN is not an active registration"));
        }
        target.setSupplierGSTIN(supplier.getGstin());
        for (TaxInvoiceLine source : input.getLines()) {
            if (source == null || source.getQuantity() == null || source.getQuantity().signum() <= 0
                    || source.getRate() == null || source.getRate().signum() < 0
                    || source.getGstRate() == null || source.getGstRate().signum() < 0
                    || source.getGstRate().compareTo(HUNDRED) > 0
                    || isNegative(source.getCess())) {
                throw badRequest("Invoice lines require positive quantity, non-negative price, and GST rate from 0 to 100");
            }
            GSTCalculationRequest request = new GSTCalculationRequest();
            request.setTaxableAmount(source.getRate().multiply(source.getQuantity()));
            request.setGstRate(source.getGstRate());
            request.setCessRate(safe(source.getCessRate()));
            request.setTaxInclusive(source.isTaxInclusive());
            request.setSupplierStateCode(requiredSupplierStateCode(supplier));
            request.setCustomerStateCode(requiredCustomerStateCode(input));
            var calculated = calculationService.calculate(request);

            TaxInvoiceLine line = new TaxInvoiceLine();
            line.setInvoice(target);
            line.setDescription(source.getDescription());
            line.setHsnSacCode(source.getHsnSacCode());
            line.setQuantity(source.getQuantity());
            line.setRate(source.getRate());
            line.setTaxInclusive(source.isTaxInclusive());
            line.setGstRate(source.getGstRate());
            line.setTaxableAmount(calculated.getTaxableAmount());
            line.setCgst(calculated.getCgst());
            line.setSgst(calculated.getSgst());
            line.setIgst(calculated.getIgst());
            line.setCessRate(safe(source.getCessRate()));
            line.setCess(calculated.getCess());
            lines.add(line);
            taxable = taxable.add(calculated.getTaxableAmount());
            cgst = cgst.add(calculated.getCgst());
            sgst = sgst.add(calculated.getSgst());
            igst = igst.add(calculated.getIgst());
            cess = cess.add(calculated.getCess());
        }
        target.setLines(lines);
        target.setTaxableAmount(taxable);
        target.setCgst(cgst);
        target.setSgst(sgst);
        target.setIgst(igst);
        target.setCess(cess);
        BigDecimal totalTax = cgst.add(sgst).add(igst).add(cess);
        target.setTotalTax(totalTax);
        target.setGrandTotal(taxable.add(totalTax));
    }

    private String requiredSupplierStateCode(GSTRegistration registration) {
        if (registration.getStateCode() == null || registration.getStateCode().isBlank()) {
            throw badRequest("Active GST registration must have a supplier state code");
        }
        return registration.getStateCode().trim();
    }

    private String requiredCustomerStateCode(TaxInvoice invoice) {
        if (invoice.getCustomerStateCode() == null
                || !invoice.getCustomerStateCode().trim().matches("\\d{2}")) {
            throw badRequest("Customer state code is required");
        }
        return invoice.getCustomerStateCode().trim();
    }

    private void validateNoteInput(CreditDebitNote note) {
        if (note == null || note.getNoteNumber() == null || note.getNoteNumber().isBlank()
                || note.getNoteDate() == null || note.getNoteType() == null) {
            throw badRequest("Note number, date, and note type are required");
        }
        if (noteRepository.findByNoteNumber(note.getNoteNumber().trim()).isPresent()) {
            throw conflict("Credit/debit note number already exists");
        }
        if (note.getOriginalInvoiceId() == null) {
            throw badRequest("Original invoice ID is required for a credit/debit note");
        }
        TaxInvoice original = findInvoice(note.getOriginalInvoiceId());
        if (!RETURNABLE_STATUSES.contains(original.getStatus())) {
            throw conflict("Notes can only reference a confirmed, active invoice");
        }
        if (original.getSourceType() == null || original.getSourceId() == null
                || original.getLines() == null || original.getLines().isEmpty()) {
            throw conflict("Returns require a GST invoice linked to a sale or purchase voucher");
        }
        String direction = original.getTransactionType() == null
                ? "SALES" : original.getTransactionType().trim().toUpperCase();
        String noteDirection = note.getTransactionType() == null
                ? direction : note.getTransactionType().trim().toUpperCase();
        if (!direction.equals(noteDirection)) {
            throw badRequest("Credit/debit note direction must match the original invoice");
        }
        boolean salesCredit = "SALES".equals(direction)
                && (note.getNoteType() == com.vpm.Accounts.Enum.NoteType.CREDIT
                || note.getNoteType() == com.vpm.Accounts.Enum.NoteType.CREDIT_NOTE);
        boolean purchaseDebit = "PURCHASE".equals(direction)
                && (note.getNoteType() == com.vpm.Accounts.Enum.NoteType.DEBIT
                || note.getNoteType() == com.vpm.Accounts.Enum.NoteType.DEBIT_NOTE);
        if (!salesCredit && !purchaseDebit) {
            throw badRequest("Sales returns require a credit note; purchase returns require a debit note");
        }
        note.setTransactionType(direction);
        if (!("SALE".equals(original.getSourceType()) || "PURCHASE".equals(original.getSourceType()))) {
            throw badRequest("Returns must reference a sale or purchase GST invoice");
        }
        validatePeriodOpen(note.getNoteDate());
        if (note.getLines() == null || note.getLines().isEmpty()) {
            throw badRequest("Return quantities for at least one original invoice line are required");
        }

        Map<Long, TaxInvoiceLine> originalLines = new java.util.HashMap<>();
        for (TaxInvoiceLine line : original.getLines()) {
            if (line.getId() != null) {
                originalLines.put(line.getId(), line);
            }
        }
        Map<Long, BigDecimal> alreadyReturned = new java.util.HashMap<>();
        for (CreditDebitNote existing : noteRepository.findByOriginalInvoiceId(original.getId())) {
            if (existing.getStatus() == InvoiceStatus.CANCELLED) continue;
            if (existing.getLines() == null) continue;
            for (CreditDebitNoteLine line : existing.getLines()) {
                alreadyReturned.merge(line.getOriginalInvoiceLineId(), safe(line.getQuantity()), BigDecimal::add);
            }
        }

        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        BigDecimal cess = BigDecimal.ZERO;
        Map<Long, BigDecimal> requestedByLine = new java.util.HashMap<>();
        List<CreditDebitNoteLine> calculatedLines = new ArrayList<>();
        for (CreditDebitNoteLine requested : note.getLines()) {
            if (requested.getOriginalInvoiceLineId() == null || requested.getQuantity() == null
                    || requested.getQuantity().signum() <= 0) {
                throw badRequest("Each returned line needs an original invoice line and a positive quantity");
            }
            TaxInvoiceLine originalLine = originalLines.get(requested.getOriginalInvoiceLineId());
            if (originalLine == null || originalLine.getProductId() == null
                    || originalLine.getQuantity() == null || originalLine.getQuantity().signum() <= 0) {
                throw badRequest("A return line does not match a returnable product line on the original invoice");
            }
            BigDecimal requestedQuantity = requestedByLine.merge(
                    originalLine.getId(), requested.getQuantity(), BigDecimal::add);
            BigDecimal returnedBefore = alreadyReturned.getOrDefault(originalLine.getId(), BigDecimal.ZERO);
            BigDecimal available = originalLine.getQuantity().subtract(returnedBefore);
            if (requestedQuantity.compareTo(available) > 0) {
                throw conflict("Return quantity exceeds the unreturned quantity for "
                        + originalLine.getDescription());
            }
            BigDecimal ratio = requested.getQuantity()
                    .divide(originalLine.getQuantity(), 10, RoundingMode.HALF_UP);
            CreditDebitNoteLine line = new CreditDebitNoteLine();
            line.setOriginalInvoiceLineId(originalLine.getId());
            line.setProductId(originalLine.getProductId());
            line.setDescription(originalLine.getDescription());
            line.setHsnSacCode(originalLine.getHsnSacCode());
            line.setQuantity(requested.getQuantity());
            line.setRate(originalLine.getRate());
            line.setGstRate(safe(originalLine.getGstRate()));
            line.setCessRate(safe(originalLine.getCessRate()));
            line.setTaxableAmount(safe(originalLine.getTaxableAmount())
                    .multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            line.setCgst(safe(originalLine.getCgst()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            line.setSgst(safe(originalLine.getSgst()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            line.setIgst(safe(originalLine.getIgst()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            line.setCess(safe(originalLine.getCess()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            calculatedLines.add(line);
            taxable = taxable.add(line.getTaxableAmount());
            cgst = cgst.add(line.getCgst());
            sgst = sgst.add(line.getSgst());
            igst = igst.add(line.getIgst());
            cess = cess.add(line.getCess());
        }
        note.setLines(calculatedLines);
        note.setTaxableAmount(taxable);
        note.setCgst(cgst);
        note.setSgst(sgst);
        note.setIgst(igst);
        note.setCess(cess);
        note.setTotal(taxable.add(cgst).add(sgst).add(igst).add(cess));
    }

    private void applyReturnEffects(CreditDebitNote note) {
        if (accountRepository == null || productRepository == null || journalEntryRepository == null
                || financialYearRepository == null || accountingPeriodRepository == null) {
            throw new IllegalStateException("Return accounting dependencies are not configured");
        }
        boolean salesReturn = "SALES".equalsIgnoreCase(note.getTransactionType());
        TaxInvoice original = findInvoice(note.getOriginalInvoiceId());
        Account party = accountRepository.findFirstByNameIgnoreCase(original.getCustomerName())
                .orElseThrow(() -> badRequest("Customer or supplier ledger account is required to confirm this return"));
        Account salesOrPurchase = accountRepository
                .findFirstByNameIgnoreCase(salesReturn ? "Sales" : "Purchases")
                .orElseThrow(() -> badRequest("Required Sales or Purchases ledger account was not found"));

        for (CreditDebitNoteLine line : note.getLines()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> notFound("Return product not found: " + line.getProductId()));
            BigDecimal currentStock = safe(product.getCurrentStockQuantity());
            if (salesReturn) {
                product.setCurrentStockQuantity(currentStock.add(line.getQuantity()));
            } else {
                if (currentStock.compareTo(line.getQuantity()) < 0) {
                    throw conflict("Purchase return quantity exceeds available stock for " + line.getDescription());
                }
                product.setCurrentStockQuantity(currentStock.subtract(line.getQuantity()));
            }
            productRepository.save(product);
        }

        FinancialYear financialYear = financialYearRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        note.getNoteDate(), note.getNoteDate())
                .orElseThrow(() -> badRequest("No financial year exists for return date: " + note.getNoteDate()));
        if (financialYear.isClosed()) {
            throw conflict("Financial year " + financialYear.getName() + " is closed");
        }
        AccountingPeriod accountingPeriod = accountingPeriodRepository
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        financialYear.getId(), note.getNoteDate(), note.getNoteDate())
                .orElseThrow(() -> badRequest("No accounting period exists for return date: " + note.getNoteDate()));
        if (accountingPeriod.isClosed()) {
            throw conflict("Accounting period " + accountingPeriod.getName() + " is closed");
        }

        JournalEntry journal = new JournalEntry();
        journal.setVoucherNumber(note.getNoteNumber());
        journal.setEntryDate(note.getNoteDate());
        journal.setFinancialYear(financialYear);
        journal.setAccountingPeriod(accountingPeriod);
        journal.setDescription((salesReturn ? "Sales return - " : "Purchase return - ")
                + original.getCustomerName() + " (" + note.getNoteNumber() + ")");

        List<JournalEntryLine> journalLines = new ArrayList<>();
        if (salesReturn) {
            addReturnJournalLine(journalLines, journal, salesOrPurchase, note.getTaxableAmount(), BigDecimal.ZERO);
            addReturnTaxLine(journalLines, journal, "Output CGST", note.getCgst(), true);
            addReturnTaxLine(journalLines, journal, "Output SGST", note.getSgst(), true);
            addReturnTaxLine(journalLines, journal, "Output IGST", note.getIgst(), true);
            addReturnTaxLine(journalLines, journal, "Output GST Cess", note.getCess(), true);
            addReturnJournalLine(journalLines, journal, party, BigDecimal.ZERO, note.getTotal());
        } else {
            addReturnJournalLine(journalLines, journal, party, note.getTotal(), BigDecimal.ZERO);
            addReturnJournalLine(journalLines, journal, salesOrPurchase, BigDecimal.ZERO, note.getTaxableAmount());
            addReturnTaxLine(journalLines, journal, "Input CGST", note.getCgst(), false);
            addReturnTaxLine(journalLines, journal, "Input SGST", note.getSgst(), false);
            addReturnTaxLine(journalLines, journal, "Input IGST", note.getIgst(), false);
            addReturnTaxLine(journalLines, journal, "Input GST Cess", note.getCess(), false);
        }
        journal.setLines(journalLines);
        journalEntryRepository.save(journal);
    }

    private void addReturnTaxLine(
            List<JournalEntryLine> lines, JournalEntry journal, String accountName,
            BigDecimal amount, boolean debit) {
        if (safe(amount).signum() == 0) return;
        Account account = accountRepository.findFirstByNameIgnoreCase(accountName)
                .orElseThrow(() -> badRequest("GST ledger account not found: " + accountName));
        addReturnJournalLine(lines, journal, account,
                debit ? amount : BigDecimal.ZERO, debit ? BigDecimal.ZERO : amount);
    }

    private void addReturnJournalLine(
            List<JournalEntryLine> lines, JournalEntry journal, Account account,
            BigDecimal debit, BigDecimal credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(safe(debit));
        line.setCredit(safe(credit));
        line.setJournalEntry(journal);
        lines.add(line);
    }

    private void validatePeriodOpen(LocalDate date) {
        for (TaxPeriod period : periodRepository.findAll()) {
            if (period.isLocked() && period.getStartDate() != null && period.getEndDate() != null
                    && !date.isBefore(period.getStartDate()) && !date.isAfter(period.getEndDate())) {
                throw conflict("Tax period is locked. Transactions cannot be modified.");
            }
        }
    }

    private TaxReconciliationDTO buildReconciliation(TaxPeriod period) {
        if (period.getStartDate() == null || period.getEndDate() == null) {
            throw badRequest("Tax period must have start and end dates before reconciliation");
        }
        String financialYear = period.getFinancialYear();
        if (financialYear == null || financialYear.isBlank() || period.getMonth() == null) {
            throw badRequest("Tax period must have financial year and month for reconciliation");
        }
        List<GSTReturn> declaredReturns =
                returnRepository.findAllByFinancialYearAndMonth(financialYear, period.getMonth());
        if (declaredReturns.isEmpty()) {
            throw notFound("Filed GST return not found for this tax period");
        }
        List<TaxInvoice> invoices = invoiceRepository.findByInvoiceDateBetweenAndStatusIn(
                period.getStartDate(), period.getEndDate(), RETURNABLE_STATUSES);
        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (TaxInvoice invoice : invoices) {
            taxable = taxable.add(safe(invoice.getTaxableAmount()));
            tax = tax.add(safe(invoice.getCgst())).add(safe(invoice.getSgst()))
                    .add(safe(invoice.getIgst())).add(safe(invoice.getCess()));
        }
        List<CreditDebitNote> notes = noteRepository.findByNoteDateBetweenAndStatus(
                period.getStartDate(), period.getEndDate(), InvoiceStatus.CONFIRMED);
        for (CreditDebitNote note : notes) {
            BigDecimal adjustment = isCreditNote(note)
                    ? BigDecimal.valueOf(-1)
                    : BigDecimal.ONE;
            taxable = taxable.add(safe(note.getTaxableAmount()).multiply(adjustment));
            BigDecimal noteTax = safe(note.getCgst()).add(safe(note.getSgst()))
                    .add(safe(note.getIgst())).add(safe(note.getCess()));
            tax = tax.add(noteTax.multiply(adjustment));
        }
        BigDecimal declaredTaxable = BigDecimal.ZERO;
        BigDecimal declaredTax = BigDecimal.ZERO;
        for (GSTReturn declared : declaredReturns) {
            declaredTaxable = declaredTaxable.add(safe(declared.getOutwardTaxable()));
            declaredTax = declaredTax.add(safe(declared.getOutwardCGST()))
                    .add(safe(declared.getOutwardSGST()))
                    .add(safe(declared.getOutwardIGST()))
                    .add(safe(declared.getOutwardCess()));
        }
        TaxReconciliationDTO dto = new TaxReconciliationDTO();
        dto.setPeriodId(period.getId());
        dto.setStatus(period.getReconciliationStatus());
        dto.setInvoiceTaxable(taxable);
        dto.setReturnTaxable(declaredTaxable);
        dto.setTaxableDifference(taxable.subtract(declaredTaxable));
        dto.setInvoiceTax(tax);
        dto.setReturnTax(declaredTax);
        dto.setTaxDifference(tax.subtract(declaredTax));
        return dto;
    }

    private YearMonth yearMonth(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw badRequest("A valid year and month are required");
        }
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw badRequest("A valid from date on or before the to date is required");
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void requireGstEnabled() {
        boolean enabled = tenantTaxSettingsRepository != null
                && tenantTaxSettingsRepository.findFirstByOrderByIdAsc()
                        .map(TenantTaxSettings::isGstEnabled).orElse(false);
        if (!enabled) {
            throw conflict("GST is disabled for this tenant. Enable it in Settings before using GST endpoints.");
        }
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }

    private boolean isCreditNote(CreditDebitNote note) {
        return note.getNoteType() == com.vpm.Accounts.Enum.NoteType.CREDIT
                || note.getNoteType() == com.vpm.Accounts.Enum.NoteType.CREDIT_NOTE;
    }

    private boolean hasActiveNotes(Long invoiceId) {
        return noteRepository.findByOriginalInvoiceId(invoiceId).stream()
                .anyMatch(note -> note.getStatus() != InvoiceStatus.CANCELLED);
    }

    private void requireGatewaySuccess(String status, String operation) {
        if (status == null || status.isBlank()
                || List.of("FAILED", "FAILURE", "ERROR", "REJECTED")
                .contains(status.trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GST gateway did not confirm " + operation);
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException unavailable(String message) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
