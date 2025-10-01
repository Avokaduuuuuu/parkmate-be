package com.parkmate.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/users/auth/**",
            "/actuator/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
    };

    // Internal endpoints - cho inter-service communication
    public static final String[] INTERNAL_ENDPOINTS = {
            "/internal/**"
    };

    // Admin only endpoints
    public static final String[] ADMIN_ENDPOINTS = {
            "/api/v1/admin/**"
    };

    // Partner only endpoints
    public static final String[] PARTNER_ENDPOINTS = {
            "/api/v1/partner/**"
    };

    // Driver/Member only endpoints
    public static final String[] MEMBER_ENDPOINTS = {
            "/api/v1/users/profile/**",
            "/api/v1/users/vehicles/**",
            "/api/user-service/partners/**",
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(INTERNAL_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}