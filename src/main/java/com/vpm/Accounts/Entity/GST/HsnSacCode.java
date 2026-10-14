package com.vpm.Accounts.Entity.GST;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.security.TenantEntityListener;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.Enum.HsnSacType;

@Entity
@Table(name = "hsn_sac_codes", uniqueConstraints = @UniqueConstraint(
        name = "uk_hsn_sac_tenant_code", columnNames = {"tenant_id", "code"}))
@EntityListeners(TenantEntityListener.class)
public class HsnSacCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    private HsnSacType type;

    private boolean active;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HsnSacType getType() {
        return type;
    }

    public void setType(HsnSacType type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @JsonIgnore
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}