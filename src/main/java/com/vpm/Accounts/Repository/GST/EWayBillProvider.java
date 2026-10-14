package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.EWayBillResponse;
import com.vpm.Accounts.Entity.GST.TaxInvoice;

public interface EWayBillProvider {

    EWayBillResponse generate(TaxInvoice invoice);

    EWayBillResponse updateVehicle(String ewbNumber, String vehicleNumber);

    EWayBillResponse cancel(String ewbNumber, String reason);
}