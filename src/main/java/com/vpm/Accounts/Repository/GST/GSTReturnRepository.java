package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.GSTReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;

public interface GSTReturnRepository extends JpaRepository<GSTReturn, Long> {
    List<GSTReturn> findAllByFinancialYearAndMonth(String financialYear, Integer month);
    List<GSTReturn> findAllByGstinAndFinancialYearAndMonth(String gstin, String financialYear, Integer month);
}
