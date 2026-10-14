package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.TaxPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface TaxPeriodRepository extends JpaRepository<TaxPeriod, Long> {
    Optional<TaxPeriod> findByDate(LocalDate date);
    List<TaxPeriod> findAllByOrderByStartDateDesc();
}
