package com.parkmate.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
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
            "/api/v1/parking-service/floors",
            "/api/v1/parking-service/areas",
            "/api/v1/parking-service/areas/{id}",
            "/api/v1/parking-service/spots",
            "/api/v1/parking-service/spots/{id}",
            "/api/v1/payment-service/momo/**",
            "/api/v1/parking-service/sessions/**",
            "/api/v1/parking-service/subscriptions/**",
            "/api/v1/parking-service/lots/*/vehicle-type/*",
            "/api/v1/parking-service/floors/*/vehicle-type/*",
            "/api/v1/parking-service/ratings",

            // PayOS webhook endpoints (must be public for PayOS callbacks)
            "/api/v1/payment-service/payos/payos_transfer_handler",
            "/api/v1/payment-service/payos/return",
            "/api/v1/payment-service/payos/cancel",

            // Internal service-to-service endpoints (bypassing gateway auth, authenticated at service level)
            "/internal/**",

            // Sync API
            "/api/v1/parking-service/pricing-rules/{lotId}/sync",
            "/api/v1/parking-service/pricing-rules/sync",
            "/api/v1/user-service/reservations/{lotId}/sync",
            "/api/v1/user-service/reservations/{id}/sync",
            "api/v1/user-service/vehicle/users/{userId}",
            "/api/v1/parking-service/sessions/{lotId}/sync",
            "/api/v1/parking-service/policies/{lotId}/sync",
            "/api/v1/parking-service/policies/sync",
            "/api/v1/user-service/user-subscriptions/{id}/sync",
            "/api/v1/user-service/user-subscriptions/{lotId}/sync",
            "/api/v1/payment-service/transactions/session-payment",

            // Test
            "/api/v1/fcm/test",
            "/api/v1/payment-service/wallets/sync",


    };


    // Partner only endpoints
    public static final String[] PARTNER_ENDPOINTS = {
            "/api/v1/user-service/partners/**",
            "/api/v1/parking-service/lots/**",
            "/api/v1/parking-service/floors/**",
            "/api/v1/parking-service/areas/**",
            "/api/v1/parking-service/spots/**",
            "/api/v1/parking-service/sessions/**",
            "/api/v1/parking-service/policies/**",
            "/api/v1/parking-service/pricing-rules/**",
            "/api/v1/parking-service/subscriptions/**",
            "/api/v1/parking-service/devices/**",
    };

    public static final String[] MEMBER_PARKING_ENDPOINTS = {
            "/api/v1/parking-service/spots/*/session",
            "/api/v1/parking-service/lots/*/available-spots",
    };

    // Member endpoints (regular users)
    public static final String[] MEMBER_ENDPOINTS = {
            "/api/v1/user-service/users/**",
            "/api/v1/user-service/vehicles/**",
            "/api/v1/user-service/reservations/**",
            "/api/v1/user-service/mobile-devices/**",
            "/api/v1/payment-service/**",
            "/api/v1/payment-service/wallets/**",
            "/api/v1/user-service/reservations/**",
            "/api/v1/parking-service/ratings/**"
    };

    @Value("${jwt.secret}")
    private String JWT_KEY;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(ex -> ex
                        // FIX 1: Allow OPTIONS for CORS preflight
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .pathMatchers(MEMBER_PARKING_ENDPOINTS).hasAnyRole("MEMBER", "ADMIN")
                        .pathMatchers(PARTNER_ENDPOINTS).hasAnyRole("PARTNER_OWNER", "PARTNER_STAFF", "ADMIN")
                        .pathMatchers(MEMBER_ENDPOINTS).hasAnyRole("MEMBER", "ADMIN", "PARTNER_OWNER", "PARTNER_STAFF")
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

        // FIX 2: Add your frontend domains
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",           // Local dev
                "http://localhost:3000",           // Local dev
                "https://park-mate-sand.vercel.app", // Vercel deployment
                "https://avokadu.com",             // Your custom domain (if frontend here)
                "https://www.avokadu.com"          // www version
        ));

        // FIX 3: Ensure OPTIONS is in allowed methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
