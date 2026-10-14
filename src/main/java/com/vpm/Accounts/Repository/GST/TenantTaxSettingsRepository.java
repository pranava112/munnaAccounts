package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.TenantTaxSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantTaxSettingsRepository extends JpaRepository<TenantTaxSettings, Long> {
    Optional<TenantTaxSettings> findFirstByOrderByIdAsc();
}
