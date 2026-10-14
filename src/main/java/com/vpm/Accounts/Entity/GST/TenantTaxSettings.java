package com.vpm.Accounts.Entity.GST;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.security.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "tenant_tax_settings")
@EntityListeners(TenantEntityListener.class)
public class TenantTaxSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gst_enabled", nullable = false)
    private boolean gstEnabled;

    @Column(name = "tax_inclusive", nullable = false)
    private boolean taxInclusive;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false, unique = true)
    private Long tenantId;

    public Long getId() { return id; }
    public boolean isGstEnabled() { return gstEnabled; }
    public void setGstEnabled(boolean gstEnabled) { this.gstEnabled = gstEnabled; }
    public boolean isTaxInclusive() { return taxInclusive; }
    public void setTaxInclusive(boolean taxInclusive) { this.taxInclusive = taxInclusive; }
    @JsonIgnore
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
