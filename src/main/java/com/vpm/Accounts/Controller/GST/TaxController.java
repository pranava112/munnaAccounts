package com.vpm.Accounts.Controller.GST;

import com.vpm.Accounts.DTO.GST.*;
import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Entity.GST.*;
import com.vpm.Accounts.Service.GST.TaxApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tax")
@CrossOrigin(origins = "*")
public class TaxController {
    private final TaxApiService taxApiService;

    public TaxController(TaxApiService taxApiService) {
        this.taxApiService = taxApiService;
    }

    @GetMapping("/settings")
    public ResponseEntity<TenantTaxSettings> taxSettings() {
        return ResponseEntity.ok(taxApiService.taxSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<TenantTaxSettings> updateTaxSettings(
            @RequestBody TenantTaxSettings settings) {
        return ResponseEntity.ok(taxApiService.updateTaxSettings(settings));
    }

    @GetMapping("/hsn-sac")
    public ResponseEntity<List<HsnSacCode>> hsnSacCodes() {
        return ResponseEntity.ok(taxApiService.hsnSacCodes());
    }

    @GetMapping("/hsn-sac/{code}")
    public ResponseEntity<HsnSacCode> hsnSacCode(@PathVariable String code) {
        return ResponseEntity.ok(taxApiService.hsnSacCode(code));
    }

    @PostMapping("/hsn-sac")
    public ResponseEntity<HsnSacCode> createHsnSacCode(@RequestBody HsnSacCode code) {
        return ResponseEntity.status(201).body(taxApiService.createHsnSacCode(code));
    }

    @PostMapping("/invoices")
    public ResponseEntity<TaxInvoice> createInvoice(@RequestBody TaxInvoice invoice) {
        return ResponseEntity.status(201).body(taxApiService.createInvoice(invoice));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<TaxInvoice>> invoices() {
        return ResponseEntity.ok(taxApiService.invoices());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<TaxInvoice> invoice(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.invoice(id));
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<TaxInvoice> updateInvoice(
            @PathVariable Long id, @RequestBody TaxInvoice invoice) {
        return ResponseEntity.ok(taxApiService.updateInvoice(id, invoice));
    }

    @PostMapping("/invoices/{id}/confirm")
    public ResponseEntity<TaxInvoice> confirmInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.confirmInvoice(id));
    }

    @PostMapping("/invoices/{id}/cancel")
    public ResponseEntity<TaxInvoice> cancelInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.cancelInvoice(id));
    }

    @PostMapping("/notes")
    public ResponseEntity<CreditDebitNote> createNote(@RequestBody CreditDebitNote note) {
        return ResponseEntity.status(201).body(taxApiService.createNote(note));
    }

    @GetMapping("/notes")
    public ResponseEntity<List<CreditDebitNote>> notes() {
        return ResponseEntity.ok(taxApiService.notes());
    }

    @GetMapping("/notes/{id}")
    public ResponseEntity<CreditDebitNote> note(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.note(id));
    }

    @PostMapping("/notes/{id}/confirm")
    public ResponseEntity<CreditDebitNote> confirmNote(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.confirmNote(id));
    }

    @PostMapping("/notes/{id}/cancel")
    public ResponseEntity<CreditDebitNote> cancelNote(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.cancelNote(id));
    }

    @GetMapping("/gstr1")
    public ResponseEntity<GSTR1SummaryDTO> gstr1(
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ResponseEntity.ok(taxApiService.gstr1(from, to));
    }

    @GetMapping("/gstr3b")
    public ResponseEntity<GSTR3BSummaryDTO> gstr3b(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(taxApiService.gstr3b(year, month));
    }

    @GetMapping("/sales-register")
    public ResponseEntity<List<TaxInvoice>> salesRegister(
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ResponseEntity.ok(taxApiService.salesRegister(from, to));
    }

    @PostMapping("/einvoice/{invoiceId}/generate")
    public ResponseEntity<EInvoiceRecord> generateEInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(taxApiService.generateEInvoice(invoiceId));
    }

    @PostMapping("/einvoice/{invoiceId}/cancel")
    public ResponseEntity<EInvoiceRecord> cancelEInvoice(
            @PathVariable Long invoiceId, @RequestBody ReasonRequest request) {
        return ResponseEntity.ok(taxApiService.cancelEInvoice(
                invoiceId, request == null ? null : request.getReason()));
    }

    @GetMapping("/einvoice/{invoiceId}")
    public ResponseEntity<EInvoiceRecord> eInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(taxApiService.eInvoice(invoiceId));
    }

    @PostMapping("/ewaybill/{invoiceId}/generate")
    public ResponseEntity<EWayBillRecord> generateEWayBill(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(taxApiService.generateEWayBill(invoiceId));
    }

    @PutMapping("/ewaybill/{id}/vehicle")
    public ResponseEntity<EWayBillRecord> updateEWayBillVehicle(
            @PathVariable Long id, @RequestBody VehicleUpdateRequest request) {
        return ResponseEntity.ok(taxApiService.updateEWayBillVehicle(
                id, request == null ? null : request.getVehicleNumber()));
    }

    @PostMapping("/ewaybill/{id}/cancel")
    public ResponseEntity<EWayBillRecord> cancelEWayBill(
            @PathVariable Long id, @RequestBody ReasonRequest request) {
        return ResponseEntity.ok(taxApiService.cancelEWayBill(
                id, request == null ? null : request.getReason()));
    }

    @PostMapping("/tds/calculate")
    public ResponseEntity<TdsTransaction> calculateTds(@RequestBody TaxCalculationRequest request) {
        return ResponseEntity.status(201).body(taxApiService.calculateWithholding("TDS", request));
    }

    @GetMapping("/tds/transactions")
    public ResponseEntity<List<TdsTransaction>> tdsTransactions() {
        return ResponseEntity.ok(taxApiService.withholdingTransactions("TDS"));
    }

    @GetMapping("/tds/report")
    public ResponseEntity<List<TdsTransaction>> tdsReport(
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ResponseEntity.ok(taxApiService.withholdingReport("TDS", from, to));
    }

    @PostMapping("/tcs/calculate")
    public ResponseEntity<TdsTransaction> calculateTcs(@RequestBody TaxCalculationRequest request) {
        return ResponseEntity.status(201).body(taxApiService.calculateWithholding("TCS", request));
    }

    @GetMapping("/tcs/transactions")
    public ResponseEntity<List<TdsTransaction>> tcsTransactions() {
        return ResponseEntity.ok(taxApiService.withholdingTransactions("TCS"));
    }

    @GetMapping("/periods")
    public ResponseEntity<List<TaxPeriod>> periods() {
        return ResponseEntity.ok(taxApiService.periods());
    }

    @PostMapping("/periods/{id}/lock")
    public ResponseEntity<TaxPeriod> lockPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.setPeriodLocked(id, true));
    }

    @PostMapping("/periods/{id}/unlock")
    public ResponseEntity<TaxPeriod> unlockPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(taxApiService.setPeriodLocked(id, false));
    }

    @GetMapping("/reconciliation/{periodId}")
    public ResponseEntity<TaxReconciliationDTO> reconciliation(@PathVariable Long periodId) {
        return ResponseEntity.ok(taxApiService.reconciliation(periodId));
    }

    @PostMapping("/reconciliation/{periodId}/reconcile")
    public ResponseEntity<TaxReconciliationDTO> reconcile(@PathVariable Long periodId) {
        return ResponseEntity.ok(taxApiService.reconcile(periodId));
    }
}
