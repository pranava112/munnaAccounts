package com.vpm.Accounts.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vpm.Accounts.Entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}