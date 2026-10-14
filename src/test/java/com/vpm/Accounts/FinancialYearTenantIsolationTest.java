package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.security.TenantContext;

@SpringBootTest
class FinancialYearTenantIsolationTest {

    @Autowired
    private FinancialYearRepository financialYearRepository;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void financialYearsAreOnlyVisibleToTheirTenant() {
        TenantContext.set(101L);
        financialYearRepository.save(year("Tenant 101", 2025));

        TenantContext.set(202L);
        financialYearRepository.save(year("Tenant 202", 2025));

        TenantContext.set(101L);
        List<FinancialYear> tenantYears =
                financialYearRepository.findAllByOrderByStartDateDesc();

        assertEquals(1, tenantYears.size());
        assertEquals("Tenant 101", tenantYears.get(0).getName());
    }

    private FinancialYear year(String name, int startYear) {
        FinancialYear year = new FinancialYear();
        year.setName(name);
        year.setStartDate(LocalDate.of(startYear, 4, 1));
        year.setEndDate(LocalDate.of(startYear + 1, 3, 31));
        year.setStatus(FinancialYear.Status.OPEN);
        return year;
    }
}
