package com.vpm.Accounts.Service.GST;

import com.vpm.Accounts.Entity.GST.TaxPeriod;
import com.vpm.Accounts.Repository.GST.TaxPeriodRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TaxPeriodService {

    private final TaxPeriodRepository repository;

    public TaxPeriodService(TaxPeriodRepository repository) {
        this.repository = repository;
    }

    public void validatePeriodOpen(LocalDate date) {
        TaxPeriod period = repository.findByDate(date)
                .orElseThrow(() -> new IllegalArgumentException("Tax period not found"));

        if (period.isLocked()) {
            throw new IllegalStateException("Tax period is locked. Transactions cannot be modified.");
        }
    }
}
