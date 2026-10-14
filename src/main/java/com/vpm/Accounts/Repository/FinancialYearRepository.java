// package com.vpm.Accounts.Repository;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.vpm.Accounts.Entity.FinancialYear;

// public interface FinancialYearRepository extends JpaRepository<FinancialYear,Long> {

//     Optional<FinancialYear> findByName(String name);

//     boolean existsByName(String name);

//     List<FinancialYear> findAllByOrderByStartDateDesc();

//     Optional<FinancialYear>
//     findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
//             LocalDate date1,
//             LocalDate date2
//     );

//     List<FinancialYear>
//     findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
//             LocalDate endDate,
//             LocalDate startDate
//     );

// //     findByFinancialYear_IdOrderByEntryDateAsc(...)

    
// }




package com.vpm.Accounts.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.FinancialYear;

public interface FinancialYearRepository
        extends JpaRepository<FinancialYear, Long> {

    // =====================================================
    // FIND BY NAME
    // =====================================================

    Optional<FinancialYear>
    findByName(String name);

    // =====================================================
    // CHECK NAME
    // =====================================================

    boolean existsByName(String name);

    // =====================================================
    // GET ALL
    // =====================================================

    List<FinancialYear>
    findAllByOrderByStartDateDesc();

    // =====================================================
    // FIND FINANCIAL YEAR BY DATE
    // =====================================================

    Optional<FinancialYear>
    findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate date1,
            LocalDate date2
    );

    // =====================================================
    // FIND OVERLAPPING FINANCIAL YEARS
    // =====================================================

    List<FinancialYear>
    findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate endDate,
            LocalDate startDate
    );
}