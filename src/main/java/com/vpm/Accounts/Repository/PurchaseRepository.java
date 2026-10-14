package com.vpm.Accounts.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.vpm.Accounts.Entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
	Optional<Purchase> findByVoucherNumber(String voucherNumber);

	boolean existsByItems_Product_Id(Long productId);

	List<Purchase> findDistinctByItems_Product_Id(Long productId);
	
	Optional<Purchase> findFirstByVoucherNumber(String voucherNumber);
}