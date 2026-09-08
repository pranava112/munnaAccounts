package com.vpm.Accounts.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.vpm.Accounts.Entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
	Optional<Purchase> findByVoucherNumber(String voucherNumber);
}