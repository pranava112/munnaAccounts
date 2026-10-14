package com.vpm.Accounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.Tenant;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.TenantRepository;
import com.vpm.Accounts.Service.Accounts_Service;
import com.vpm.Accounts.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DefaultChartOfAccountsTest {
    @Autowired
    private Accounts_Service accountsService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void initializesStandardChartOncePerTenantAndSupportsCustomContactFields() {
        Tenant tenant = new Tenant();
        tenant.setName("Chart Test");
        tenant.setSlug("chart-test");
        tenant = tenantRepository.save(tenant);
        Long tenantId = tenant.getId();

        accountsService.initializeDefaultAccounts(tenantId);
        TenantContext.set(tenantId);
        long initialCount = accountRepository.count();
        assertThat(initialCount).isGreaterThan(10);
        assertThat(accountRepository.findFirstByNameIgnoreCase("Sales"))
                .get()
                .extracting(Account::getType)
                .isEqualTo(AccountType.INCOME);
        assertThat(accountRepository.findFirstByNameIgnoreCase("Sales"))
                .get()
                .extracting(Account::getAccountGroup)
                .isEqualTo("SALES");

        accountsService.initializeDefaultAccounts(tenantId);
        TenantContext.set(tenantId);
        assertThat(accountRepository.count()).isEqualTo(initialCount);

        Account debtor = new Account();
        debtor.setName("Customer account");
        debtor.setType(AccountType.ASSET);
        debtor.setAccountGroup("DEBTORS");
        debtor.setPhoneNumber("5550100");
        debtor.setAddress("1 Main Street");
        debtor.setCustomFields(Map.of("GSTIN", "TEST-GSTIN"));
        Account saved = accountRepository.save(debtor);
        accountRepository.flush();

        Account loaded = accountRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getPhoneNumber()).isEqualTo("5550100");
        assertThat(loaded.getAddress()).isEqualTo("1 Main Street");
        assertThat(loaded.getCustomFields()).containsEntry("GSTIN", "TEST-GSTIN");
    }
}
