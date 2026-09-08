# Multi-tenant rollout

The application now assigns every company a tenant ID and every financial record to that tenant. Existing rows created before tenant isolation have a null `tenant_id` and are intentionally hidden until they are assigned. Do this once before production:

1. Register the first company through `POST /api/auth/register` and note its returned `tenantId`.
2. Back up the database.
3. Assign the existing single-company data to that tenant. Replace `TENANT_ID` with the returned value:

```sql
UPDATE accounts SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE products SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE purchase SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE sale SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE journal_entry SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE journal_entry_line SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
UPDATE users SET tenant_id = TENANT_ID WHERE tenant_id IS NULL;
```

Do not run those statements after multiple companies have already been created unless the rows are known to belong to the same company. There is deliberately no automatic claim of legacy rows because that could expose one firm's accounts to another.

## Runtime configuration

Set `JWT_SECRET` to a random value of at least 32 bytes and configure `ALLOWED_ORIGINS` with the deployed frontend origins. Access tokens expire after 15 minutes by default; refresh tokens expire after 7 days and are rotated and revoked on every refresh.

An admin creates additional members through `POST /api/admin/members`. The role is always `USER`; clients cannot self-register as `ADMIN`.
