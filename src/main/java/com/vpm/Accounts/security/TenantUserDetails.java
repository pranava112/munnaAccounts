package com.vpm.Accounts.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.vpm.Accounts.Entity.Users;

public class TenantUserDetails implements UserDetails {
    private final Users user;
    public TenantUserDetails(Users user) { this.user = user; }
    public Users user() { return user; }
    public Long tenantId() { return user.getTenantId(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole() == null ? "VIEWER" : user.getRole().replaceFirst("^ROLE_", "").toUpperCase();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isEnabled(); }
}
