# Multi-tenant rollout

Business records are isolated using Hibernate's tenant discriminator, resolved from the authenticated JWT for each request. This applies to accounts, products, sales and purchases (including their items), journal entries and lines, financial years, accounting periods, and audit logs. During startup and unauthenticated requests, the resolver uses reserved tenant ID `0` so Hibernate can initialize; generated tenant IDs must remain positive. New or updated business records still require an authenticated tenant, and no tenant-owned rows should be assigned ID `0`.

Existing rows whose `tenant_id` is null are not visible to any tenant. Before deploying this change:

1. Back up the database.
2. Determine which company owns each existing row and register/create that tenant.
3. Assign each legacy row to its actual tenant. For a database that was used by a single company, replace `TENANT_ID` with that company's ID:

```sql
UPDATE account SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE products SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE purchase SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE purchase_item SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE sale SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE sale_item SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE journal_entry SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE journal_entry_line SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE financial_years SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE accounting_periods SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE audit_logs SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
```

Do not run a single-tenant backfill after multiple companies exist unless ownership is known per row. Do not backfill by guessing: that could expose one firm's financial records to another. The Hibernate schema updater adds the discriminator columns but does not determine record ownership.

The schema uses tenant-scoped unique keys for financial year names, product barcodes, sales and purchase voucher numbers, and journal voucher numbers. When upgrading a database that already has their old globally unique indexes, replace those indexes with the corresponding tenant-scoped indexes after backing up the schema; `ddl-auto=update` does not reliably remove old indexes.

New sales and purchase voucher numbers are assigned by the backend in the form `SV-{tenantId}-{year}-{recordId}` and `PV-{tenantId}-{year}-{recordId}`. Opening-stock journal vouchers also include the tenant ID. A product can be deleted only if no sales or purchases reference it; deleting an unused product also removes its opening-stock journal entry.

Financial years are April 1 through March 31. Reports, trial balances, and ledgers must be viewed for a selected financial year. Closing a year creates year-end profit/loss and balance c/d journal entries; creating the following year automatically posts balance b/d entries for permanent accounts. Net profit or loss is transferred to the tenant's Capital account. A closed year cannot be reopened once the following year contains transactions.

## Runtime configuration

Set `JWT_SECRET` to a random value of at least 32 bytes and configure `ALLOWED_ORIGINS` with the deployed frontend origins. Access tokens expire after 15 minutes by default; refresh tokens expire after 7 days and are rotated and revoked on every refresh.

An admin creates additional members through `POST /api/admin/members`. The role is always `USER`; clients cannot self-register as `ADMIN`.
