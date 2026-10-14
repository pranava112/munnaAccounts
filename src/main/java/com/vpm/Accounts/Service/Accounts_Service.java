package com.vpm.Accounts.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.Tenant;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryLineRepository;
import com.vpm.Accounts.Repository.TenantRepository;
import com.vpm.Accounts.security.TenantContext;

// import jakarta.transaction.Transactional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class Accounts_Service {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final TenantRepository tenantRepository;
    private final TransactionTemplate transactionTemplate;

    public Accounts_Service(AccountRepository accountRepository,
            JournalEntryLineRepository journalEntryLineRepository,
            TenantRepository tenantRepository,
            TransactionTemplate transactionTemplate) {
        this.accountRepository = accountRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.tenantRepository = tenantRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void initializeDefaultAccounts(@NonNull Long tenantId) {
        TenantContext.set(tenantId);
        try {
            transactionTemplate.execute(status -> {
                Tenant tenant = tenantRepository.findById(tenantId)
                        .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
                if (tenant.isDefaultChartInitialized()) {
                    return null;
                }

                for (DefaultAccount definition : defaultChart()) {
                    Optional<Account> existingAccount =
                            accountRepository.findFirstByNameIgnoreCase(definition.name());
                    if (existingAccount.isEmpty()) {
                        Account account = new Account();
                        account.setName(definition.name());
                        account.setType(definition.type());
                        account.setAccountGroup(definition.group());
                        account.setBalance(java.math.BigDecimal.ZERO);
                        accountRepository.save(account);
                    } else if (existingAccount.get().getAccountGroup() == null
                            || existingAccount.get().getAccountGroup().isBlank()) {
                        Account account = existingAccount.get();
                        account.setAccountGroup(definition.group());
                        accountRepository.save(account);
                    }
                }

                tenant.setDefaultChartInitialized(true);
                tenantRepository.save(tenant);
                return null;
            });
        } finally {
            TenantContext.clear();
        }
    }

    @Async("accountExecutor")
    public CompletableFuture<Account> saveAccountAsync(
            @NonNull Long tenantId,
            @NonNull Account account) {
        return executeForTenant(tenantId, () -> accountRepository.save(account));
    }

    @Async("accountExecutor")
    public CompletableFuture<Optional<Account>> getAccountByIdAsync(
            @NonNull Long tenantId,
            @NonNull Long id) {
        return executeForTenant(tenantId, () -> accountRepository.findById(id));
    }

    @Async("accountExecutor")
    public CompletableFuture<Account> updateAccountAsync(
            @NonNull Long tenantId,
            @NonNull Long id,
            @NonNull Account account) {
        return executeForTenant(tenantId, () -> {
            return accountRepository.findById(id)
                .map(existing -> {
                    existing.setName(account.getName());
                    existing.setType(account.getType());
                    existing.setCode(account.getCode());
                    existing.setAccountGroup(account.getAccountGroup());
                    existing.setContactPerson(account.getContactPerson());
                    existing.setPhoneNumber(account.getPhoneNumber());
                    existing.setEmail(account.getEmail());
                    existing.setAddress(account.getAddress());
                    existing.setGstin(account.getGstin());
                    existing.setState(account.getState());
                    existing.setStateCode(account.getStateCode());
                    existing.setCustomFields(account.getCustomFields());
                    return accountRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));
        });
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteAccountAsync(
            @NonNull Long tenantId,
            @NonNull Long id) {
        return executeForTenant(tenantId, () -> {
            if (journalEntryLineRepository.existsByAccount_Id(id)) {
                throw new IllegalStateException(
                        "Account cannot be deleted because it is used by journal entries");
            }

            accountRepository.deleteById(id);
            return null;
        });
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Account>> getAllAccountsAsync(
            @NonNull Long tenantId) {
        initializeDefaultAccounts(tenantId);
        return executeForTenant(tenantId, () -> (List<Account>) accountRepository.findAll());
    }

    @Async("accountExecutor")
    public CompletableFuture<Optional<Account>> findByNameAsync(
            @NonNull Long tenantId,
            @NonNull String name) {
        return executeForTenant(tenantId, () -> accountRepository.findFirstByName(name));
    }

    // Read-only hint for optimization
    @Transactional(readOnly = true)
    public Optional<Account> findAllByName(String name) {
        return accountRepository.findFirstByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findAllByNameIgnoreCase(String name) {
        return accountRepository.findFirstByNameIgnoreCase(name);
    }

    private <T> CompletableFuture<T> executeForTenant(
            Long tenantId,
            Supplier<T> operation) {
        TenantContext.set(tenantId);
        try {
            T result = transactionTemplate.execute(status -> operation.get());
            return CompletableFuture.completedFuture(result);
        } finally {
            TenantContext.clear();
        }
    }

    private List<DefaultAccount> defaultChart() {
        return List.of(
                new DefaultAccount("Cash", AccountType.ASSET, "CASH"),
                new DefaultAccount("Bank", AccountType.ASSET, "BANK"),
                new DefaultAccount("Sundry Debtors", AccountType.ASSET, "DEBTORS"),
                new DefaultAccount("Stock in Hand", AccountType.ASSET, "STOCK"),
                new DefaultAccount("Opening Stock", AccountType.OPENING_STOCK, "OPENING_STOCK"),
                new DefaultAccount("Sundry Creditors", AccountType.LIABILITY, "CREDITORS"),
                new DefaultAccount("Duties & Taxes", AccountType.LIABILITY, "DUTIES_TAXES"),
                new DefaultAccount("Capital", AccountType.CAPITAL, "CAPITAL"),
                new DefaultAccount("Drawings", AccountType.DRAWINGS, "DRAWINGS"),
                new DefaultAccount("Sales", AccountType.INCOME, "SALES"),
                new DefaultAccount("Direct Income", AccountType.INCOME, "DIRECT_INCOME"),
                new DefaultAccount("Indirect Income", AccountType.INCOME, "INDIRECT_INCOME"),
                new DefaultAccount("Purchases", AccountType.EXPENSE, "PURCHASES"),
                new DefaultAccount("Direct Expenses", AccountType.EXPENSE, "DIRECT_EXPENSES"),
                new DefaultAccount("Indirect Expenses", AccountType.EXPENSE, "INDIRECT_EXPENSES"));
    }

    private record DefaultAccount(String name, AccountType type, String group) {}

    public Optional<Account> findById(Long id) {
            return accountRepository.findById(id);
     }

     public List<Account> findAllActiveAccounts() {
        // Account has no active flag yet, so all tenant accounts are returned.
        return accountRepository.findAll();
    }
}