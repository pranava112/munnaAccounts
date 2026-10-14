package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.EInvoiceResponse;
import com.vpm.Accounts.Entity.GST.TaxInvoice;

public interface EInvoiceProvider {

    EInvoiceResponse generate(TaxInvoice invoice);

    EInvoiceResponse cancel(String irn, String reason);
}
