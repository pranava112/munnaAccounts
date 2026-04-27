//package com.vpm.Accounts.Repository;
//
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.vpm.Accounts.Entity.Account;
//import com.vpm.Accounts.Entity.Product;
//
//public interface AccountRepository extends JpaRepository<Account,Long> {
//
////	Optional<Product> findByName(String string);
//
//}



package com.vpm.Accounts.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vpm.Accounts.Entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // ✅ THIS IS REQUIRED
    Optional<Account> findByName(String name);

    // ✅ Case-insensitive fallback for more robust lookup
    Optional<Account> findByNameIgnoreCase(String name);
}