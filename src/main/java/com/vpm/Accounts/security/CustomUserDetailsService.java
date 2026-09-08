package com.vpm.Accounts.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.vpm.Accounts.Repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repository;
    public CustomUserDetailsService(UserRepository repository) { this.repository = repository; }
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new TenantUserDetails(repository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password")));
    }
}
