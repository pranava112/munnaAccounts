package com.vpm.Accounts.security;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TenantRepositorySupport<T> {
    List<T> findAllByTenantId(Long tenantId);
    Optional<T> findByIdAndTenantId(Long id, Long tenantId);
    void deleteByIdAndTenantId(Long id, Long tenantId);
}
