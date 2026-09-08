// package com.vpm.Accounts.Controller;

// import java.nio.charset.StandardCharsets;
// import java.security.MessageDigest;
// import java.security.SecureRandom;
// import java.time.Instant;
// import java.util.Base64;
// import java.util.Locale;
// import java.util.UUID;
// import jakarta.validation.Valid;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.server.ResponseStatusException;
// import com.vpm.Accounts.DTO.AuthRequest;
// import com.vpm.Accounts.DTO.LoginRequest;
// import com.vpm.Accounts.DTO.RefreshRequest;
// import com.vpm.Accounts.DTO.TokenResponse;
// import com.vpm.Accounts.Entity.RefreshSession;
// import com.vpm.Accounts.Entity.Tenant;
// import com.vpm.Accounts.Entity.Users;
// import com.vpm.Accounts.Repository.RefreshSessionRepository;
// import com.vpm.Accounts.Repository.TenantRepository;
// import com.vpm.Accounts.Repository.UserRepository;
// import com.vpm.Accounts.security.JwtService;
// import com.vpm.Accounts.security.TenantUserDetails;

// @RestController
// @RequestMapping("/api/auth")
// public class AuthController {
//     private final AuthenticationManager authenticationManager;
//     private final UserRepository users;
//     private final TenantRepository tenants;
//     private final RefreshSessionRepository sessions;
//     private final PasswordEncoder encoder;
//     private final JwtService jwt;
//     private final long refreshDays;
//     private final SecureRandom random = new SecureRandom();

//     public AuthController(AuthenticationManager authenticationManager, UserRepository users, TenantRepository tenants,
//             RefreshSessionRepository sessions, PasswordEncoder encoder, JwtService jwt,
//             @Value("${app.security.refresh-token-days:7}") long refreshDays) {
//         this.authenticationManager = authenticationManager; this.users = users; this.tenants = tenants;
//         this.sessions = sessions; this.encoder = encoder; this.jwt = jwt; this.refreshDays = refreshDays;
//     }

//     @PostMapping("/register")
//     @ResponseStatus(HttpStatus.CREATED)
//     public TokenResponse register(@Valid @RequestBody AuthRequest request) {
//         if (users.existsByUsername(request.getUsername())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
//         if (users.existsByEmail(request.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
//         Tenant tenant = new Tenant(); tenant.setName(request.getCompanyName().trim()); tenant.setSlug(slug(request.getCompanyName()));
//         if (tenants.findBySlug(tenant.getSlug()).isPresent()) tenant.setSlug(tenant.getSlug() + "-" + UUID.randomUUID().toString().substring(0, 8));
//         tenant = tenants.save(tenant);
//         Users user = new Users(); user.setUsername(request.getUsername().trim()); user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
//         user.setPassword(encoder.encode(request.getPassword())); user.setRole("OWNER"); user.setTenantId(tenant.getId()); user.setEnabled(true);
//         users.save(user);
//         return issue(user);
//     }

//     @PostMapping("/login")
//     public TokenResponse login(@Valid @RequestBody LoginRequest request) {
//         Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
//         return issue(((TenantUserDetails) auth.getPrincipal()).user());
//     }

//     @PostMapping("/refresh")
//     public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
//         String hash = hash(request.refreshToken());
//         RefreshSession old = sessions.findByTokenHash(hash).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
//         if (old.isRevoked() || old.getExpiresAt().isBefore(Instant.now())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
//         old.setRevoked(true); sessions.save(old);
//         Users user = users.findByUsername(old.getUsername()).filter(value -> old.getTenantId().equals(value.getTenantId()) && value.isEnabled())
//             .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is unavailable"));
//         return issue(user);
//     }

//     @PostMapping("/logout")
//     @ResponseStatus(HttpStatus.NO_CONTENT)
//     public void logout(@Valid @RequestBody RefreshRequest request) {
//         sessions.findByTokenHash(hash(request.refreshToken())).ifPresent(session -> { session.setRevoked(true); sessions.save(session); });
//     }

//     private TokenResponse issue(Users user) {
//         TenantUserDetails details = new TenantUserDetails(user);
//         String refresh = randomToken();
//         RefreshSession session = new RefreshSession(); session.setTokenHash(hash(refresh)); session.setUsername(user.getUsername());
//         session.setTenantId(user.getTenantId()); session.setExpiresAt(Instant.now().plusSeconds(refreshDays * 86400)); session.setRevoked(false); sessions.save(session);
//         return new TokenResponse(jwt.accessToken(details), refresh, user.getTenantId(), user.getUsername(), user.getRole());
//     }
//     private String randomToken() { byte[] bytes = new byte[48]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
//     private String hash(String value) { try { return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
//     private String slug(String value) { return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""); }
// }



package com.vpm.Accounts.Controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.vpm.Accounts.DTO.AuthRequest;
import com.vpm.Accounts.DTO.LoginRequest;
import com.vpm.Accounts.DTO.RefreshRequest;
import com.vpm.Accounts.DTO.TokenResponse;
import com.vpm.Accounts.Entity.RefreshSession;
import com.vpm.Accounts.Entity.Tenant;
import com.vpm.Accounts.Entity.Users;
import com.vpm.Accounts.Repository.RefreshSessionRepository;
import com.vpm.Accounts.Repository.TenantRepository;
import com.vpm.Accounts.Repository.UserRepository;
import com.vpm.Accounts.security.JwtService;
import com.vpm.Accounts.security.TenantUserDetails;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final TenantRepository tenants;
    private final RefreshSessionRepository sessions;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final long refreshDays;

    private final SecureRandom random = new SecureRandom();

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository users,
            TenantRepository tenants,
            RefreshSessionRepository sessions,
            PasswordEncoder encoder,
            JwtService jwt,
            @Value("${app.security.refresh-token-days:7}")
            long refreshDays) {

        this.authenticationManager = authenticationManager;
        this.users = users;
        this.tenants = tenants;
        this.sessions = sessions;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refreshDays = refreshDays;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(
            @Valid @RequestBody AuthRequest request) {

        String username =
                request.getUsername().trim();

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String companyName =
                request.getCompanyName().trim();

        // Global uniqueness
        if (users.existsByUsername(username)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (users.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        // =====================================================
        // CREATE TENANT
        // =====================================================

        Tenant tenant = new Tenant();

        tenant.setName(companyName);

        String tenantSlug =
                slug(companyName);

        if (tenants.findBySlug(tenantSlug).isPresent()) {

            tenantSlug =
                    tenantSlug + "-"
                    + UUID.randomUUID()
                            .toString()
                            .substring(0, 8);
        }

        tenant.setSlug(tenantSlug);

        tenant = tenants.save(tenant);

        // =====================================================
        // CREATE OWNER
        // =====================================================

        Users user = new Users();

        user.setUsername(username);
        user.setEmail(email);

        user.setPassword(
                encoder.encode(request.getPassword())
        );

        user.setRole("OWNER");

        user.setTenantId(tenant.getId());

        user.setEnabled(true);

        users.save(user);

        // =====================================================
        // ISSUE JWT + REFRESH TOKEN
        // =====================================================

        return issue(user);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request) {

        String username =
                request.getUsername().trim();

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                username,
                                request.getPassword()
                        )
                );

        TenantUserDetails details =
                (TenantUserDetails) authentication.getPrincipal();

        Users user = details.user();

        if (!user.isEnabled()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User account is disabled"
            );
        }

        return issue(user);
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @PostMapping("/refresh")
    public TokenResponse refresh(
            @Valid @RequestBody RefreshRequest request) {

        String hash =
                hash(request.refreshToken());

        RefreshSession old =
                sessions.findByTokenHash(hash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token"
                                )
                        );

        if (old.isRevoked()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has been revoked"
            );
        }

        if (old.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired"
            );
        }

        // Rotate refresh token
        old.setRevoked(true);

        sessions.save(old);

        Users user =
                users.findByUsername(old.getUsername())
                        .filter(value ->
                                old.getTenantId()
                                        .equals(value.getTenantId())
                                        && value.isEnabled()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User is unavailable"
                                )
                        );

        return issue(user);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshRequest request) {

        String hash =
                hash(request.refreshToken());

        sessions.findByTokenHash(hash)
                .ifPresent(session -> {

                    session.setRevoked(true);

                    sessions.save(session);
                });
    }

    // =========================================================
    // ISSUE TOKENS
    // =========================================================

    private TokenResponse issue(Users user) {

        TenantUserDetails details =
                new TenantUserDetails(user);

        // Generate refresh token
        String refresh =
                randomToken();

        // Store only SHA-256 hash
        RefreshSession session =
                new RefreshSession();

        session.setTokenHash(
                hash(refresh)
        );

        session.setUsername(
                user.getUsername()
        );

        session.setTenantId(
                user.getTenantId()
        );

        session.setExpiresAt(
                Instant.now()
                        .plusSeconds(
                                refreshDays * 86400L
                        )
        );

        session.setRevoked(false);

        sessions.save(session);

        // Generate access token
        String accessToken =
                jwt.accessToken(details);

        return new TokenResponse(
                accessToken,
                refresh,
                user.getTenantId(),
                user.getUsername(),
                user.getRole()
        );
    }

    // =========================================================
    // RANDOM REFRESH TOKEN
    // =========================================================

    private String randomToken() {

        byte[] bytes =
                new byte[48];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // =========================================================
    // HASH
    // =========================================================

    private String hash(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to hash refresh token",
                    e
            );
        }
    }

    // =========================================================
    // SLUG
    // =========================================================

    private String slug(String value) {

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}