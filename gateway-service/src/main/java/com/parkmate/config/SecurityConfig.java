package com.parkmate.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            //Upload
            "/api/v1/user-service/upload/**",

            // Auth endpoints
            "/api/v1/user-service/auth/**",
            "/api/v1/user-service/partner-registrations/**",

            // Documentation
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/aggregate/**",
            "/webjars/**",

            //TEST
            "/api/v1/user-service/tokens/**",

            // Public user endpoints (read-only)
            "/api/v1/user-service/users",
            "/api/v1/parking-service/lots",
            "/api/v1/parking-service/lots/{id}",
            "/api/v1/parking-service/floors/{id}",
            "/api/v1/payment-service/momo/**",
            "/api/v1/parking-service/sessions/**"
    };


    // Partner only endpoints
    public static final String[] PARTNER_ENDPOINTS = {
            "/api/v1/user-service/partners/**",
            "/api/v1/parking-service/**",
    };

    // Member endpoints (regular users)
    public static final String[] MEMBER_ENDPOINTS = {
            "/api/v1/user-service/users/**",
            "/api/v1/user-service/vehicles/**",
            "/api/v1/user-service/reservations/**",
            "/api/v1/user-service/mobile-devices/**",
            "/api/v1/payment-service/**"
    };

    @Value("${jwt.secret}")
    private String JWT_KEY;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Changed this line
                .authorizeExchange(ex -> ex
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .pathMatchers(PARTNER_ENDPOINTS).hasAnyRole("PARTNER_OWNER", "PARTNER_STAFF", "ADMIN")
                        .pathMatchers(MEMBER_ENDPOINTS).hasAnyRole("MEMBER", "ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())))
                .build();
    }

    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>>
    grantedAuthoritiesExtractor() {
        var delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Example: roles in a custom "roles" claim
            String role = jwt.getClaimAsString("role");
            System.out.println("JWT Claims: " + jwt.getClaims());
            System.out.println("Extracted role: " + role);
            if (role == null) return List.of();
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            System.out.println("Final authority: " + authority);
            return List.of(new SimpleGrantedAuthority(authority));
        });
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = JWT_KEY.getBytes();
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");
        return NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
