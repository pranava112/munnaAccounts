// package com.vpm.Accounts.Service;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.vpm.Accounts.Entity.AccountingPeriod;
// import com.vpm.Accounts.Entity.FinancialYear;
// import com.vpm.Accounts.Repository.AccountingPeriodRepository;
// import com.vpm.Accounts.Repository.FinancialYearRepository;

// @Service
// public class AccountingPeriodService {

//     private final AccountingPeriodRepository periodRepo;
//     private final FinancialYearRepository financialYearRepo;

//     public AccountingPeriodService(
//             AccountingPeriodRepository periodRepo,
//             FinancialYearRepository financialYearRepo) {

//         this.periodRepo = periodRepo;
//         this.financialYearRepo = financialYearRepo;
//     }

//     // =====================================================
//     // CREATE PERIOD
//     // =====================================================

//     @Transactional
//     public AccountingPeriod createPeriod(
//             Long financialYearId,
//             AccountingPeriod request) {

//         FinancialYear year =
//                 financialYearRepo.findById(
//                         financialYearId
//                 ).orElseThrow(() ->
//                         new RuntimeException(
//                                 "Financial year not found"));

//         if (year.isClosed()) {
//             throw new IllegalStateException(
//                     "Cannot create period in a closed financial year");
//         }

//         validatePeriodDates(request);

//         if (request.getStartDate()
//                 .isBefore(year.getStartDate())
//                 ||
//                 request.getEndDate()
//                         .isAfter(year.getEndDate())) {

//             throw new IllegalArgumentException(
//                     "Accounting period must be completely "
//                             + "inside the financial year");
//         }

//         String name =
//                 request.getName();

//         if (name == null || name.isBlank()) {
//             name =
//                     request.getStartDate()
//                             .toString();
//         }

//         if (periodRepo.existsByFinancialYearIdAndName(
//                 financialYearId,
//                 name
//         )) {

//             throw new IllegalArgumentException(
//                     "Accounting period already exists: "
//                             + name);
//         }

//         List<AccountingPeriod> overlapping =
//                 periodRepo
//                         .findByFinancialYearIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                                 financialYearId,
//                                 request.getEndDate(),
//                                 request.getStartDate()
//                         );

//         if (!overlapping.isEmpty()) {
//             throw new IllegalStateException(
//                     "Accounting period overlaps "
//                             + "with an existing period");
//         }

//         AccountingPeriod period =
//                 new AccountingPeriod();

//         period.setName(name);
//         period.setCode(
//                 request.getCode() == null
//                         || request.getCode().isBlank()
//                         ? generateCode(request)
//                         : request.getCode().trim()
//         );

//         period.setStartDate(
//                 request.getStartDate()
//         );

//         period.setEndDate(
//                 request.getEndDate()
//         );

//         period.setStatus(
//                 AccountingPeriod.Status.OPEN
//         );

//         period.setFinancialYear(year);

//         return periodRepo.save(period);
//     }

//     // =====================================================
//     // GET ALL BY FINANCIAL YEAR
//     // =====================================================

//     @Transactional(readOnly = true)
//     public List<AccountingPeriod>
//     getPeriodsByFinancialYear(
//             Long financialYearId) {

//         if (!financialYearRepo.existsById(
//                 financialYearId)) {

//             throw new RuntimeException(
//                     "Financial year not found");
//         }

//         return periodRepo
//                 .findByFinancialYearIdOrderByStartDateAsc(
//                         financialYearId
//                 );
//     }

//     // =====================================================
//     // GET BY ID
//     // =====================================================

//     @Transactional(readOnly = true)
//     public AccountingPeriod getPeriodById(
//             Long id) {

//         return periodRepo.findById(id)
//                 .orElseThrow(() ->
//                         new RuntimeException(
//                                 "Accounting period not found with id: "
//                                         + id));
//     }

//     // =====================================================
//     // GET PERIOD BY DATE
//     // =====================================================

//     @Transactional(readOnly = true)
//     public AccountingPeriod getPeriodByDate(
//             LocalDate date) {

//         if (date == null) {
//             throw new IllegalArgumentException(
//                     "Date is required");
//         }

//         return periodRepo
//                 .findFirstByFinancialYearIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                         findFinancialYearId(date),
//                         date,
//                         date
//                 )
//                 .orElseThrow(() ->
//                         new RuntimeException(
//                                 "No accounting period exists for date: "
//                                         + date));
//     }

//     // =====================================================
//     // CLOSE PERIOD
//     // =====================================================

//     @Transactional
//     public AccountingPeriod closePeriod(
//             Long id) {

//         AccountingPeriod period =
//                 getPeriodById(id);

//         FinancialYear year =
//                 period.getFinancialYear();

//         if (year == null) {
//             throw new IllegalStateException(
//                     "Period is not linked to a financial year");
//         }

//         if (year.isClosed()) {
//             throw new IllegalStateException(
//                     "Financial year is already closed");
//         }

//         if (period.isClosed()) {
//             throw new IllegalStateException(
//                     "Accounting period is already closed");
//         }

//         period.setStatus(
//                 AccountingPeriod.Status.CLOSED
//         );

//         period.setClosedAt(
//                 LocalDateTime.now()
//         );

//         return periodRepo.save(period);
//     }

//     // =====================================================
//     // REOPEN PERIOD
//     // =====================================================

//     @Transactional
//     public AccountingPeriod reopenPeriod(
//             Long id) {

//         AccountingPeriod period =
//                 getPeriodById(id);

//         FinancialYear year =
//                 period.getFinancialYear();

//         if (year == null) {
//             throw new IllegalStateException(
//                     "Period is not linked to a financial year");
//         }

//         if (year.isClosed()) {
//             throw new IllegalStateException(
//                     "Cannot reopen a period while "
//                             + "the financial year is closed");
//         }

//         if (period.isOpen()) {
//             throw new IllegalStateException(
//                     "Accounting period is already open");
//         }

//         period.setStatus(
//                 AccountingPeriod.Status.OPEN
//         );

//         period.setClosedAt(null);

//         return periodRepo.save(period);
//     }

//     // =====================================================
//     // INTERNAL HELPERS
//     // =====================================================

//     private Long findFinancialYearId(
//             LocalDate date) {

//         return financialYearRepo
//                 .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                         date,
//                         date
//                 )
//                 .orElseThrow(() ->
//                         new RuntimeException(
//                                 "No financial year exists for date: "
//                                         + date
//                         ))
//                 .getId();
//     }

//     private String generateCode(
//             AccountingPeriod request) {

//         return request.getStartDate()
//                 .toString()
//                 + "_"
//                 + request.getEndDate()
//                         .toString();
//     }

//     private void validatePeriodDates(
//             AccountingPeriod period) {

//         if (period.getStartDate() == null) {
//             throw new IllegalArgumentException(
//                     "Period start date is required");
//         }

//         if (period.getEndDate() == null) {
//             throw new IllegalArgumentException(
//                     "Period end date is required");
//         }

//         if (period.getStartDate()
//                 .isAfter(period.getEndDate())) {

//             throw new IllegalArgumentException(
//                     "Period start date cannot be "
//                             + "after end date");
//         }
//     }
// }



package com.vpm.Accounts.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Repository.AccountingPeriodRepository;
import com.vpm.Accounts.Repository.FinancialYearRepository;

@Service
public class AccountingPeriodService {

    private final AccountingPeriodRepository periodRepo;
    private final FinancialYearRepository financialYearRepo;

    public AccountingPeriodService(
            AccountingPeriodRepository periodRepo,
            FinancialYearRepository financialYearRepo) {

        this.periodRepo = periodRepo;
        this.financialYearRepo = financialYearRepo;
    }

    // =====================================================
    // CREATE PERIOD
    // =====================================================

    @Transactional
    public AccountingPeriod createPeriod(
            Long financialYearId,
            AccountingPeriod request) {

        if (financialYearId == null) {
            throw new IllegalArgumentException(
                    "Financial year ID is required"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Accounting period data is required"
            );
        }

        // -------------------------------------------------
        // FIND FINANCIAL YEAR
        // -------------------------------------------------

        FinancialYear year =
                financialYearRepo.findById(
                        financialYearId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Financial year not found with id: "
                                        + financialYearId
                        )
                );

        // -------------------------------------------------
        // FINANCIAL YEAR MUST BE OPEN
        // -------------------------------------------------

        if (year.isClosed()) {

            throw new IllegalStateException(
                    "Cannot create accounting period "
                            + "inside a closed financial year"
            );
        }

        // -------------------------------------------------
        // VALIDATE DATES
        // -------------------------------------------------

        validatePeriodDates(request);

        // -------------------------------------------------
        // PERIOD MUST BE INSIDE FINANCIAL YEAR
        // -------------------------------------------------

        if (request.getStartDate()
                .isBefore(year.getStartDate())) {

            throw new IllegalArgumentException(
                    "Accounting period start date "
                            + "cannot be before financial year start date"
            );
        }

        if (request.getEndDate()
                .isAfter(year.getEndDate())) {

            throw new IllegalArgumentException(
                    "Accounting period end date "
                            + "cannot be after financial year end date"
            );
        }

        // -------------------------------------------------
        // NAME
        // -------------------------------------------------

        String name = request.getName();

        if (name == null || name.isBlank()) {

            name =
                    request.getStartDate()
                            .toString();
        }

        name = name.trim();

        // -------------------------------------------------
        // DUPLICATE NAME
        // -------------------------------------------------

        if (periodRepo.existsByFinancialYear_IdAndName(
                financialYearId,
                name
        )) {

            throw new IllegalArgumentException(
                    "Accounting period already exists: "
                            + name
            );
        }

        // -------------------------------------------------
        // CHECK OVERLAPPING PERIOD
        // -------------------------------------------------

        List<AccountingPeriod> overlapping =
                periodRepo
                        .findByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                financialYearId,
                                request.getEndDate(),
                                request.getStartDate()
                        );

        if (!overlapping.isEmpty()) {

            throw new IllegalStateException(
                    "Accounting period overlaps "
                            + "with an existing period"
            );
        }

        // -------------------------------------------------
        // CREATE ENTITY
        // -------------------------------------------------

        AccountingPeriod period =
                new AccountingPeriod();

        period.setName(name);

        // -------------------------------------------------
        // CODE
        // -------------------------------------------------

        String code = request.getCode();

        if (code == null || code.isBlank()) {

            code = generateCode(request);
        }

        period.setCode(code.trim());

        // -------------------------------------------------
        // DATES
        // -------------------------------------------------

        period.setStartDate(
                request.getStartDate()
        );

        period.setEndDate(
                request.getEndDate()
        );

        // -------------------------------------------------
        // DEFAULT STATUS
        // -------------------------------------------------

        period.setStatus(
                AccountingPeriod.Status.OPEN
        );

        // -------------------------------------------------
        // LINK FINANCIAL YEAR
        // -------------------------------------------------

        period.setFinancialYear(year);

        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        return periodRepo.save(period);
    }

    // =====================================================
    // GET ALL PERIODS FOR FINANCIAL YEAR
    // =====================================================

    @Transactional(readOnly = true)
    public List<AccountingPeriod>
    getPeriodsByFinancialYear(
            Long financialYearId) {

        if (financialYearId == null) {

            throw new IllegalArgumentException(
                    "Financial year ID is required"
            );
        }

        if (!financialYearRepo.existsById(
                financialYearId
        )) {

            throw new RuntimeException(
                    "Financial year not found with id: "
                            + financialYearId
            );
        }

        return periodRepo
                .findByFinancialYear_IdOrderByStartDateAsc(
                        financialYearId
                );
    }

    // =====================================================
    // GET PERIOD BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public AccountingPeriod getPeriodById(
            Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Accounting period ID is required"
            );
        }

        return periodRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Accounting period not found with id: "
                                        + id
                        )
                );
    }

    // =====================================================
    // GET PERIOD BY DATE
    // =====================================================

    @Transactional(readOnly = true)
    public AccountingPeriod getPeriodByDate(
            LocalDate date) {

        if (date == null) {

            throw new IllegalArgumentException(
                    "Date is required"
            );
        }

        Long financialYearId =
                findFinancialYearId(date);

        return periodRepo
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        financialYearId,
                        date,
                        date
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No accounting period exists "
                                        + "for date: "
                                        + date
                        )
                );
    }

    // =====================================================
    // GET OPEN PERIOD BY DATE
    // =====================================================

    @Transactional(readOnly = true)
    public AccountingPeriod getOpenPeriodByDate(
            LocalDate date) {

        if (date == null) {

            throw new IllegalArgumentException(
                    "Date is required"
            );
        }

        Long financialYearId =
                findFinancialYearId(date);

        return periodRepo
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                        financialYearId,
                        date,
                        date,
                        AccountingPeriod.Status.OPEN
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No OPEN accounting period exists "
                                        + "for date: "
                                        + date
                        )
                );
    }

    // =====================================================
    // CLOSE PERIOD
    // =====================================================

    @Transactional
    public AccountingPeriod closePeriod(
            Long id) {

        AccountingPeriod period =
                getPeriodById(id);

        FinancialYear year =
                period.getFinancialYear();

        // -------------------------------------------------
        // VALIDATE RELATIONSHIP
        // -------------------------------------------------

        if (year == null) {

            throw new IllegalStateException(
                    "Accounting period is not linked "
                            + "to a financial year"
            );
        }

        // -------------------------------------------------
        // YEAR MUST BE OPEN
        // -------------------------------------------------

        if (year.isClosed()) {

            throw new IllegalStateException(
                    "Financial year "
                            + year.getName()
                            + " is already closed"
            );
        }

        // -------------------------------------------------
        // PERIOD MUST BE OPEN
        // -------------------------------------------------

        if (period.isClosed()) {

            throw new IllegalStateException(
                    "Accounting period "
                            + period.getName()
                            + " is already closed"
            );
        }

        // -------------------------------------------------
        // CLOSE
        // -------------------------------------------------

        period.setStatus(
                AccountingPeriod.Status.CLOSED
        );

        period.setClosedAt(
                LocalDateTime.now()
        );

        return periodRepo.save(period);
    }

    // =====================================================
    // REOPEN PERIOD
    // =====================================================

    @Transactional
    public AccountingPeriod reopenPeriod(
            Long id) {

        AccountingPeriod period =
                getPeriodById(id);

        FinancialYear year =
                period.getFinancialYear();

        // -------------------------------------------------
        // VALIDATE RELATIONSHIP
        // -------------------------------------------------

        if (year == null) {

            throw new IllegalStateException(
                    "Accounting period is not linked "
                            + "to a financial year"
            );
        }

        // -------------------------------------------------
        // YEAR MUST BE OPEN
        // -------------------------------------------------

        if (year.isClosed()) {

            throw new IllegalStateException(
                    "Cannot reopen accounting period because "
                            + "the financial year is closed"
            );
        }

        // -------------------------------------------------
        // PERIOD MUST BE CLOSED
        // -------------------------------------------------

        if (period.isOpen()) {

            throw new IllegalStateException(
                    "Accounting period "
                            + period.getName()
                            + " is already open"
            );
        }

        // -------------------------------------------------
        // REOPEN
        // -------------------------------------------------

        period.setStatus(
                AccountingPeriod.Status.OPEN
        );

        period.setClosedAt(null);

        return periodRepo.save(period);
    }

    // =====================================================
    // CHECK WHETHER PERIOD IS OPEN
    // =====================================================

    @Transactional(readOnly = true)
    public boolean isPeriodOpen(
            Long id) {

        AccountingPeriod period =
                getPeriodById(id);

        FinancialYear year =
                period.getFinancialYear();

        if (year == null) {
            return false;
        }

        return year.isOpen()
                && period.isOpen();
    }

    // =====================================================
    // CHECK WHETHER DATE IS POSTABLE
    // =====================================================

    @Transactional(readOnly = true)
    public boolean isDatePostable(
            LocalDate date) {

        if (date == null) {
            return false;
        }

        try {

            getOpenPeriodByDate(date);

            return true;

        } catch (RuntimeException ex) {

            return false;
        }
    }

    // =====================================================
    // FIND FINANCIAL YEAR ID BY DATE
    // =====================================================

    private Long findFinancialYearId(
            LocalDate date) {

        return financialYearRepo
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        date,
                        date
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No financial year exists "
                                        + "for date: "
                                        + date
                        )
                )
                .getId();
    }

    // =====================================================
    // GENERATE CODE
    // =====================================================

    private String generateCode(
            AccountingPeriod request) {

        return request.getStartDate()
                + "_"
                + request.getEndDate();
    }

    // =====================================================
    // VALIDATE PERIOD DATES
    // =====================================================

    private void validatePeriodDates(
            AccountingPeriod period) {

        if (period == null) {

            throw new IllegalArgumentException(
                    "Accounting period is required"
            );
        }

        if (period.getStartDate() == null) {

            throw new IllegalArgumentException(
                    "Period start date is required"
            );
        }

        if (period.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "Period end date is required"
            );
        }

        if (period.getStartDate()
                .isAfter(period.getEndDate())) {

            throw new IllegalArgumentException(
                    "Period start date cannot be "
                            + "after period end date"
            );
        }
    }
}