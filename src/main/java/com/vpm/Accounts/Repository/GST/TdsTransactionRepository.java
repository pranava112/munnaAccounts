package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.TdsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TdsTransactionRepository extends JpaRepository<TdsTransaction, Long> {
    List<TdsTransaction> findByTypeAndTransactionDateBetweenOrderByTransactionDateDesc(
            String type, LocalDate from, LocalDate to);

    List<TdsTransaction> findByTypeOrderByTransactionDateDesc(String type);
}
