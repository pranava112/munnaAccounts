package com.vpm.Accounts.Repository.GST;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.GST.GSTRegistration;
import com.vpm.Accounts.Entity.GST.TaxInvoice;
import com.vpm.Accounts.Enum.InvoiceStatus;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface TaxInvoiceRepository extends JpaRepository<TaxInvoice, Long>  {

    @EntityGraph(attributePaths = "lines")
    List<TaxInvoice> findAllByOrderByInvoiceDateDesc();

    @EntityGraph(attributePaths = "lines")
    Optional<TaxInvoice> findById(Long id);

    Optional<TaxInvoice> findByInvoiceNumber(String invoiceNumber);

    Optional<TaxInvoice> findBySourceTypeAndSourceId(String sourceType, Long sourceId);

    List<TaxInvoice> findBySourceTypeAndSourceIdIn(String sourceType, Collection<Long> sourceIds);

    List<TaxInvoice> findByTransactionTypeAndInvoiceDateBetweenAndStatusIn(
            String transactionType, LocalDate from, LocalDate to,
            Collection<InvoiceStatus> statuses);

    List<TaxInvoice> findByInvoiceDateBetweenAndStatusIn(
            LocalDate from, LocalDate to, Collection<InvoiceStatus> statuses);

    List<TaxInvoice>
findByInvoiceDateBetweenAndStatus(
        LocalDate from,
        LocalDate to,
        InvoiceStatus status);

    
}
