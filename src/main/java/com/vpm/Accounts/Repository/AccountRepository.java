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

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Use first matching account to avoid non-unique result errors
    Optional<Account> findFirstByName(String name);
    Optional<Account> findFirstByNameIgnoreCase(String name);

    // If there are duplicate names, allow listing all matches for diagnostics/fallback
    List<Account> findAllByName(String name);
    List<Account> findAllByNameIgnoreCase(String name);

}