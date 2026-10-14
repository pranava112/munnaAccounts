package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.EInvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EInvoiceRecordRepository extends JpaRepository<EInvoiceRecord, Long> {
    Optional<EInvoiceRecord> findByInvoiceId(Long invoiceId);
}
