// package com.vpm.Accounts.security;

// public final class TenantContext {
//     private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

//     private TenantContext() { }

//     public static void set(Long tenantId) { CURRENT.set(tenantId); }
//     public static Long get() { return CURRENT.get(); }
//     public static Long require() {
//         Long tenantId = CURRENT.get();
//         if (tenantId == null) throw new IllegalStateException("Authenticated tenant is required");
//         return tenantId;
//     }
//     public static void clear() { CURRENT.remove(); }
// }



package com.vpm.Accounts.security;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT =
            new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                "Tenant ID cannot be null"
            );
        }

        CURRENT.set(tenantId);
    }

    public static Long get() {
        return CURRENT.get();
    }

    public static Long require() {

        Long tenantId = CURRENT.get();

        if (tenantId == null) {
            throw new IllegalStateException(
                "Authenticated tenant is required"
            );
        }

        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}