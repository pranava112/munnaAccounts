package com.vpm.Accounts.Controller.Accounts;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Service.AccountingPeriodService;

@RestController
@RequestMapping("/api/accounting-periods")
public class AccountingPeriodController {

    private final AccountingPeriodService service;

    public AccountingPeriodController(
            AccountingPeriodService service) {

        this.service = service;
    }

    // =====================================================
    // CREATE PERIOD
    // =====================================================

    @PostMapping("/financial-year/{financialYearId}")
    public ResponseEntity<?> create(
            @PathVariable Long financialYearId,
            @RequestBody AccountingPeriod request) {

        try {

            AccountingPeriod saved =
                    service.createPeriod(
                            financialYearId,
                            request
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(saved);

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(ex.getMessage());

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

    // =====================================================
    // GET BY FINANCIAL YEAR
    // =====================================================

    @GetMapping("/financial-year/{financialYearId}")
    public ResponseEntity<?> getByFinancialYear(
            @PathVariable Long financialYearId) {

        try {

            List<AccountingPeriod> periods =
                    service.getPeriodsByFinancialYear(
                            financialYearId
                    );

            return ResponseEntity.ok(periods);

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    service.getPeriodById(id)
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // =====================================================
    // GET BY DATE
    // =====================================================

    @GetMapping("/by-date")
    public ResponseEntity<?> getByDate(
            @RequestParam LocalDate date) {

        try {

            return ResponseEntity.ok(
                    service.getPeriodByDate(
                            date
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // =====================================================
    // CLOSE PERIOD
    // =====================================================

    @PutMapping("/{id}/close")
    public ResponseEntity<?> close(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    service.closePeriod(id)
            );

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

    // =====================================================
    // REOPEN PERIOD
    // =====================================================

    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopen(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    service.reopenPeriod(id)
            );

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }
}