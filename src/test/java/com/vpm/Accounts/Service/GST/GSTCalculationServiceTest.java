package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.DTO.GST.GSTCalculationRequest;
import com.vpm.Accounts.DTO.GST.GSTCalculationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GSTCalculationServiceTest {
    private final GSTCalculationService service = new GSTCalculationService();

    @Test
    void calculatesIntraStateTaxForTaxExclusiveAmount() {
        GSTCalculationResponse response = service.calculate(request(
                "100", "18", false, "29", "29", null));

        assertEquals(new BigDecimal("9.00"), response.getCgst());
        assertEquals(new BigDecimal("9.00"), response.getSgst());
        assertEquals(new BigDecimal("0"), response.getIgst());
        assertEquals(new BigDecimal("118.00"), response.getInvoiceTotal());
    }

    @Test
    void extractsTaxFromInclusiveAmountIncludingCess() {
        GSTCalculationResponse response = service.calculate(request(
                "119", "18", true, "29", "29", "1"));

        assertEquals(new BigDecimal("100.00"), response.getTaxableAmount());
        assertEquals(new BigDecimal("9.00"), response.getCgst());
        assertEquals(new BigDecimal("9.00"), response.getSgst());
        assertEquals(new BigDecimal("1.00"), response.getCess());
        assertEquals(new BigDecimal("119.00"), response.getInvoiceTotal());
    }

    @Test
    void calculatesInterStateSupplyAsIgst() {
        GSTCalculationResponse response = service.calculate(request(
                "100", "18", false, "29", "07", null));

        assertEquals(new BigDecimal("18.00"), response.getIgst());
        assertEquals(new BigDecimal("0"), response.getCgst());
        assertEquals(new BigDecimal("0"), response.getSgst());
        assertEquals("INTER_STATE", response.getTaxType());
    }

    @Test
    void rejectsNullRequestsAndOutOfRangeRates() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(null));
        assertThrows(IllegalArgumentException.class, () -> service.calculate(
                request("100", "101", false, "29", "29", null)));
    }

    private GSTCalculationRequest request(
            String amount,
            String rate,
            boolean inclusive,
            String supplierState,
            String customerState,
            String cessRate) {
        GSTCalculationRequest request = new GSTCalculationRequest();
        request.setTaxableAmount(new BigDecimal(amount));
        request.setGstRate(new BigDecimal(rate));
        request.setTaxInclusive(inclusive);
        request.setSupplierStateCode(supplierState);
        request.setCustomerStateCode(customerState);
        request.setCessRate(cessRate == null ? null : new BigDecimal(cessRate));
        return request;
    }
}
