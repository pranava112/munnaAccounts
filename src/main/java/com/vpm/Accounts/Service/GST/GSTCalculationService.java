package com.vpm.Accounts.Service.GST;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.vpm.Accounts.DTO.GST.GSTCalculationRequest;
import com.vpm.Accounts.DTO.GST.GSTCalculationResponse;

@Service

public class GSTCalculationService {

    private static final int SCALE=2;

    public GSTCalculationResponse calculate(GSTCalculationRequest request){
        validate(request);

        BigDecimal amount = request.getTaxableAmount();
        BigDecimal rate = request.getGstRate();
        BigDecimal cessRate = request.getCessRate() == null
                ? BigDecimal.ZERO
                : request.getCessRate();

        BigDecimal taxableAmount;

        if(request.isTaxInclusive()){
            BigDecimal divisor = BigDecimal.valueOf(100).add(rate).add(cessRate);
            taxableAmount = amount.multiply(BigDecimal.valueOf(100))
                    .divide(divisor, 6, RoundingMode.HALF_UP);
        }else{
            taxableAmount = amount;
        }

        taxableAmount = taxableAmount.setScale(SCALE,RoundingMode.HALF_UP);

        BigDecimal totalTax = taxableAmount.multiply(rate)
                .divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);

        BigDecimal cgst = BigDecimal.ZERO;

        BigDecimal sgst = BigDecimal.ZERO;

        BigDecimal igst = BigDecimal.ZERO;

        boolean intraState = request.getSupplierStateCode().trim()
                .equalsIgnoreCase(request.getCustomerStateCode().trim());

        if(intraState){
            cgst = totalTax .divide( BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_UP);

            sgst = totalTax.subtract(cgst);

        }
        else{igst = totalTax;}

         BigDecimal cess =
                taxableAmount
                        .multiply(cessRate)
                        .divide(
                                BigDecimal.valueOf(100),
                                SCALE,
                                RoundingMode.HALF_UP);

        BigDecimal finalTax =
                totalTax.add(cess);

        BigDecimal invoiceTotal =
                taxableAmount.add(finalTax);

        GSTCalculationResponse response =
                new GSTCalculationResponse();

        response.setTaxableAmount(
                taxableAmount);

        response.setCgst(cgst);

        response.setSgst(sgst);

        response.setIgst(igst);

        response.setCess(cess);

        response.setTotalTax(finalTax);

        response.setInvoiceTotal(invoiceTotal);

        response.setTaxType(
                intraState ? "INTRA_STATE" : "INTER_STATE");

        return response;
    }

    private void validate(
            GSTCalculationRequest request) {

        if (request == null || request.getTaxableAmount() == null ||
                request.getTaxableAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Invalid taxable amount");
        }

        if (request.getGstRate() == null ||
                request.getGstRate()
                        .compareTo(BigDecimal.ZERO) < 0 ||
                request.getGstRate().compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new IllegalArgumentException(
                    "Invalid GST rate");
        }

        if (request.getSupplierStateCode() == null
                || !request.getSupplierStateCode().trim().matches("\\d{2}")
                || request.getCustomerStateCode() == null
                || !request.getCustomerStateCode().trim().matches("\\d{2}")) {

            throw new IllegalArgumentException(
                    "Supplier and customer state are required");
        }
        if (request.getCessRate() != null && (request.getCessRate().signum() < 0
                || request.getCessRate().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Invalid cess rate");
        }
    

        
    }
    
}
