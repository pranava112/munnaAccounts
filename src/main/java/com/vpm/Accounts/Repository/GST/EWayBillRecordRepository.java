package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.EWayBillRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EWayBillRecordRepository extends JpaRepository<EWayBillRecord, Long> {
    Optional<EWayBillRecord> findByInvoiceId(Long invoiceId);
}
