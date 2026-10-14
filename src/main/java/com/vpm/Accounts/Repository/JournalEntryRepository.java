



// // package com.vpm.Accounts.Repository;

// // import java.util.Optional;

// // import org.springframework.data.jpa.repository.JpaRepository;
// // import org.springframework.data.jpa.repository.Modifying;
// // import org.springframework.transaction.annotation.Transactional;

// // import com.vpm.Accounts.Entity.JournalEntry;

// // public interface JournalEntryRepository
// //         extends JpaRepository<JournalEntry, Long> {

// //     Optional<JournalEntry> findByVoucherNumber(
// //             String voucherNumber
// //     );

// //     @Modifying
// //     @Transactional

// //     void deleteByVoucherNumber(
// //             String voucherNumber
// //     );
// // }





// package com.vpm.Accounts.Repository;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.transaction.annotation.Transactional;

// import com.vpm.Accounts.Entity.JournalEntry;

// public interface JournalEntryRepository
//         extends JpaRepository<JournalEntry, Long> {

//     Optional<JournalEntry> findByVoucherNumber(
//             String voucherNumber
//     );

// //     List<JournalEntry>
// //     findByFinancialYearIdOrderByEntryDateAsc(
// //             Long financialYearId
// //     );

// List<JournalEntry> findByFinancialYear_IdOrderByEntryDateAsc(Long financialYearId);


//     List<JournalEntry>
//     findByAccountingPeriodIdOrderByEntryDateAsc(
//             Long accountingPeriodId
//     );

//     @Modifying
//     @Transactional
//     void deleteByVoucherNumber(
//             String voucherNumber
//     );
// }

package com.vpm.Accounts.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.Entity.JournalEntry;

public interface JournalEntryRepository
        extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByVoucherNumber(
            String voucherNumber
    );

    // =========================================
    // FINANCIAL YEAR
    // =========================================

    List<JournalEntry>
    findByFinancialYear_IdOrderByEntryDateAsc(
            Long financialYearId
    );

    // =========================================
    // ACCOUNTING PERIOD
    // =========================================

    List<JournalEntry>
    findByAccountingPeriod_IdOrderByEntryDateAsc(
            Long accountingPeriodId
    );

    // =========================================
    // DELETE
    // =========================================

    @Modifying
    @Transactional
    void deleteByVoucherNumber(
            String voucherNumber
    );
}

