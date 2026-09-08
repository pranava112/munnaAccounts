// package com.vpm.Accounts.Repository;

// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.vpm.Accounts.Entity.Product;

// public interface ProductRepository extends JpaRepository<Product, Long> {
	
//     Optional<Product> findByBarcode(String barcode);
	
// }


package com.vpm.Accounts.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);
}