package com.vpm.Accounts.Repository;

//import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.Sale;
//import com.vpm.Accounts.Entity.Account;

public interface SaleRepository extends JpaRepository<Sale,Long> {
	
//	 Optional<Account> findByName(String name);

}
