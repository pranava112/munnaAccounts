package com.vpm.Accounts.Repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.Sale;
//import com.vpm.Accounts.Entity.Account;

public interface SaleRepository extends JpaRepository<Sale,Long> {

    boolean existsByItems_Product_Id(Long productId);

    List<Sale> findDistinctByItems_Product_Id(Long productId);
	
//	 Optional<Account> findByName(String name);

    Optional<Sale> findFirstByVoucherNumber(String voucherNumber);

}
