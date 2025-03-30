package com.vpm.Accounts.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.vpm.Accounts.Entity.Register;

public interface RegisterRepository extends JpaRepository<Register, Integer> {
}
