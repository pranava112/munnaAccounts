package com.vpm.Accounts.Entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "refresh_sessions", indexes = @Index(name = "idx_refresh_hash", columnList = "tokenHash", unique = true))
public class RefreshSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String tokenHash;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private Long tenantId;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean revoked;
    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public String getTokenHash() { return tokenHash; } public void setTokenHash(String value) { tokenHash = value; }
    public String getUsername() { return username; } public void setUsername(String value) { username = value; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long value) { tenantId = value; }
    public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant value) { expiresAt = value; }
    public boolean isRevoked() { return revoked; } public void setRevoked(boolean value) { revoked = value; }
}
