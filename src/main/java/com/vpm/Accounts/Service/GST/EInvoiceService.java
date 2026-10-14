package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.Entity.GST.EInvoiceResponse;
import com.vpm.Accounts.Entity.GST.TaxInvoice;
import com.vpm.Accounts.Repository.GST.EInvoiceProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EInvoiceService {

    private final ObjectProvider<EInvoiceProvider> providers;

    public EInvoiceService(ObjectProvider<EInvoiceProvider> providers) {
        this.providers = providers;
    }

    public EInvoiceResponse generate(TaxInvoice invoice) {
        validateInvoice(invoice);
        EInvoiceProvider provider = providers.getIfAvailable();
        if (provider == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No EInvoiceProvider is configured");
        }
        return provider.generate(invoice);
    }

    private void validateInvoice(TaxInvoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice is required");
        }
        if (invoice.getCustomerGSTIN() == null || invoice.getCustomerGSTIN().isBlank()) {
            throw new IllegalArgumentException("Customer GSTIN required");
        }
        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            throw new IllegalArgumentException("Invoice must contain items");
        }
    }
}
