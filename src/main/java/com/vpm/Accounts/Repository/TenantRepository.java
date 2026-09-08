// package com.vpm.Accounts.Repository;

// import java.util.Optional;
// import org.springframework.data.jpa.repository.JpaRepository;
// import com.vpm.Accounts.Entity.Tenant;

// public interface TenantRepository extends JpaRepository<Tenant, Long> {
//     Optional<Tenant> findBySlug(String slug);
// }



package com.vpm.Accounts.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlug(String slug);
}