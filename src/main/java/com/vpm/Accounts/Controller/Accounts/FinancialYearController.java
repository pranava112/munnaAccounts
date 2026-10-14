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

import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Service.FinancialYearService;

@RestController
@RequestMapping("/api/financial-years")
public class FinancialYearController {

    private final FinancialYearService service;

    public FinancialYearController(
            FinancialYearService service) {

        this.service = service;
    }

    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody FinancialYear request) {

        try {

            FinancialYear saved =
                    service.createFinancialYear(
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
    // GET ALL
    // =====================================================

    @GetMapping
    public ResponseEntity<List<FinancialYear>>
    getAll() {

        return ResponseEntity.ok(
                service.getAllFinancialYears()
        );
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    service.getFinancialYearById(
                            id
                    )
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
                    service.getFinancialYearByDate(
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
    // CLOSE YEAR
    // =====================================================

    @PutMapping("/{id}/close")
    public ResponseEntity<?> close(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    service.closeFinancialYear(
                            id
                    )
            );

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

    // =====================================================
    // REOPEN YEAR
    // =====================================================

    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopen(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    service.reopenFinancialYear(
                            id
                    )
            );

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

    // =====================================================
    // CREATE NEXT FINANCIAL YEAR
    // =====================================================

    @PostMapping("/{id}/create-next")
    public ResponseEntity<?> createNext(
            @PathVariable Long id) {

        try {

            FinancialYear nextYear =
                    service.createNextFinancialYear(
                            id
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(nextYear);

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
}