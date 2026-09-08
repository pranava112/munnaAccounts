package com.vpm.Accounts.security;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final CustomUserDetailsService users;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, CustomUserDetailsService users) {
        this.jwtFilter = jwtFilter;
        this.users = users;
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                .requestMatchers("/", "/error").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/user/**").hasRole("OWNER")
                .requestMatchers(HttpMethod.GET, "/api/accounts/**", "/api/products/**").hasAnyRole("OWNER", "ACCOUNTANT", "AUDITOR", "VIEWER", "SALES_USER", "PURCHASE_USER")
                .requestMatchers(HttpMethod.POST, "/api/sales/**").hasAnyRole("OWNER", "ACCOUNTANT", "SALES_USER")
                .requestMatchers(HttpMethod.PUT, "/api/sales/**").hasAnyRole("OWNER", "ACCOUNTANT", "SALES_USER")
                .requestMatchers(HttpMethod.DELETE, "/api/sales/**").hasAnyRole("OWNER", "ACCOUNTANT", "SALES_USER")
                .requestMatchers(HttpMethod.POST, "/api/purchase/**").hasAnyRole("OWNER", "ACCOUNTANT", "PURCHASE_USER")
                .requestMatchers(HttpMethod.PUT, "/api/purchase/**").hasAnyRole("OWNER", "ACCOUNTANT", "PURCHASE_USER")
                .requestMatchers(HttpMethod.DELETE, "/api/purchase/**").hasAnyRole("OWNER", "ACCOUNTANT", "PURCHASE_USER")
                .requestMatchers(HttpMethod.POST, "/api/journal/**").hasAnyRole("OWNER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.PUT, "/api/journal/**").hasAnyRole("OWNER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.DELETE, "/api/journal/**").hasAnyRole("OWNER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.POST, "/api/accounts/**", "/api/products/**").hasAnyRole("OWNER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.PUT, "/api/accounts/**", "/api/products/**").hasAnyRole("OWNER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.DELETE, "/api/accounts/**", "/api/products/**").hasAnyRole("OWNER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/reports/**", "/api/journal/**", "/api/sales/**", "/api/purchase/**").hasAnyRole("OWNER", "ACCOUNTANT", "AUDITOR", "VIEWER", "SALES_USER", "PURCHASE_USER")
                .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.security.allowed-origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(origins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
