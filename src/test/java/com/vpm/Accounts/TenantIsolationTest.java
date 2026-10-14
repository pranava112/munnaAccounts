package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.security.TenantContext;
import com.vpm.Accounts.security.TenantEntityListener;
import com.vpm.Accounts.security.TenantIdentifierResolver;

class TenantIsolationTest {

    private final TenantEntityListener entityListener =
            new TenantEntityListener();
    private final TenantIdentifierResolver tenantResolver =
            new TenantIdentifierResolver();

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void resolverUsesCurrentAuthenticatedTenant() {
        TenantContext.set(27L);

        assertEquals(27L, tenantResolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void resolverUsesBootstrapTenantWhenNoRequestIsAuthenticated() {
        assertEquals(0L, tenantResolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void newBusinessEntitiesAreAssignedToCurrentTenant() {
        TenantContext.set(27L);
        Account account = new Account();

        entityListener.assignTenant(account);

        assertEquals(27L, account.getTenantId());
    }

    @Test
    void updatesCannotChangeBusinessEntityOwnership() {
        TenantContext.set(27L);
        Account account = new Account();
        account.setTenantId(31L);

        assertThrows(
                SecurityException.class,
                () -> entityListener.validateTenant(account)
        );
    }
}
