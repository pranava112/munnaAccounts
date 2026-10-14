// package com.vpm.Accounts.Repository;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.vpm.Accounts.Entity.AccountingPeriod;
// import com.vpm.Accounts.Entity.AccountingPeriod.Status;

// public interface AccountingPeriodRepository
//         extends JpaRepository<AccountingPeriod, Long> {

//     List<AccountingPeriod>
//     findByFinancialYearIdOrderByStartDateAsc(
//             Long financialYearId
//     );

//     Optional<AccountingPeriod>
//     findFirstByFinancialYearIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//             Long financialYearId,
//             LocalDate date1,
//             LocalDate date2
//     );

//     Optional<AccountingPeriod>
//     findFirstByFinancialYearIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
//             Long financialYearId,
//             LocalDate date1,
//             LocalDate date2,
//             Status status
//     );

//     List<AccountingPeriod>
//     findByFinancialYearIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//             Long financialYearId,
//             LocalDate endDate,
//             LocalDate startDate
//     );

//     boolean existsByFinancialYearIdAndName(
//             Long financialYearId,
//             String name
//     );

//     Optional<AccountingPeriod>
//     findFirstByFinancialYearIdOrderByStartDateAsc(
//             Long financialYearId
//     );

//     Optional<AccountingPeriod>
//     findFirstByFinancialYearIdOrderByStartDateDesc(
//             Long financialYearId
//     );
// }




package com.vpm.Accounts.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.AccountingPeriod;

public interface AccountingPeriodRepository
        extends JpaRepository<AccountingPeriod, Long> {

    // =====================================================
    // GET ALL PERIODS OF A FINANCIAL YEAR
    // =====================================================

    List<AccountingPeriod>
    findByFinancialYear_IdOrderByStartDateAsc(
            Long financialYearId
    );

    // =====================================================
    // FIND PERIOD BY DATE
    // =====================================================

    Optional<AccountingPeriod>
    findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long financialYearId,
            LocalDate date1,
            LocalDate date2
    );

    // =====================================================
    // FIND OPEN/CLOSED PERIOD BY DATE
    // =====================================================

    Optional<AccountingPeriod>
    findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            Long financialYearId,
            LocalDate date1,
            LocalDate date2,
            AccountingPeriod.Status status
    );

    // =====================================================
    // FIND OVERLAPPING PERIODS
    // =====================================================

    List<AccountingPeriod>
    findByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long financialYearId,
            LocalDate endDate,
            LocalDate startDate
    );

    // =====================================================
    // CHECK DUPLICATE NAME
    // =====================================================

    boolean existsByFinancialYear_IdAndName(
            Long financialYearId,
            String name
    );

    // =====================================================
    // FIRST PERIOD
    // =====================================================

    Optional<AccountingPeriod>
    findFirstByFinancialYear_IdOrderByStartDateAsc(
            Long financialYearId
    );

    // =====================================================
    // LAST PERIOD
    // =====================================================

    Optional<AccountingPeriod>
    findFirstByFinancialYear_IdOrderByStartDateDesc(
            Long financialYearId
    );
}