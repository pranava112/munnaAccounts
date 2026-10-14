package com.vpm.Accounts.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<Long> {

    private static final Long BOOTSTRAP_TENANT_ID = 0L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? BOOTSTRAP_TENANT_ID : tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
