package com.vpm.Accounts.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vpm.Accounts.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
	
}