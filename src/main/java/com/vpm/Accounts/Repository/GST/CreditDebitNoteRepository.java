package com.vpm.Accounts.Repository.GST;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import com.vpm.Accounts.Entity.CreditDebitNote;
import com.vpm.Accounts.Enum.InvoiceStatus;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface CreditDebitNoteRepository extends JpaRepository<CreditDebitNote,Long>{
    @EntityGraph(attributePaths = "lines")
    @Query("select note from CreditDebitNote note")
    List<CreditDebitNote> findAllWithLines();

    @Override
    @EntityGraph(attributePaths = "lines")
    Optional<CreditDebitNote> findById(Long id);

    List<CreditDebitNote> findByNoteDateBetweenAndStatus(LocalDate from, LocalDate to, InvoiceStatus status);
    Optional<CreditDebitNote> findByNoteNumber(String noteNumber);
    List<CreditDebitNote> findByOriginalInvoiceId(Long originalInvoiceId);
    List<CreditDebitNote> findByOriginalInvoiceIdIn(List<Long> originalInvoiceIds);
}
