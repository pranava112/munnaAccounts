package com.vpm.Accounts.security;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.lang.reflect.Method;

public class TenantEntityListener {
    @PrePersist
    public void assignTenant(Object entity) { setTenant(entity, TenantContext.require()); }

    @PreUpdate
    public void validateTenant(Object entity) {
        Long tenantId = TenantContext.require();
        try {
            Method getter = entity.getClass().getMethod("getTenantId");
            Object existing = getter.invoke(entity);
            if (existing != null && !tenantId.equals(existing)) {
                throw new SecurityException("Tenant ownership violation");
            }
            setTenant(entity, tenantId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Tenant field is missing", exception);
        }
    }

    private void setTenant(Object entity, Long tenantId) {
        try {
            entity.getClass().getMethod("setTenantId", Long.class).invoke(entity, tenantId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Tenant field is missing", exception);
        }
    }
}
