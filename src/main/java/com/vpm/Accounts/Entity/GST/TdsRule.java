package com.vpm.Accounts.Entity.GST;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tds_rules")
public class TdsRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String section;

    private String description;

    private BigDecimal rate;

    private BigDecimal threshold;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String payeeType;

    private boolean active;

    // getters/setters
}
