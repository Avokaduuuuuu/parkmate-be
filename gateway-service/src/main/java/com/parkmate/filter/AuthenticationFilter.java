package com.parkmate.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                        // Extract userId from JWT claims
                        Object userId = jwt.getClaim("userId");
                        Object role = jwt.getClaim("role");

                        if (userId != null) {
                            // Add custom headers to forward to downstream services
                            ServerWebExchange modifiedExchange = exchange.mutate()
                                    .request(r -> r.header("X-User-Id", userId.toString())
                                            .header("X-User-Role", role != null ? role.toString() : ""))
                                    .build();
                            return chain.filter(modifiedExchange);
                        }
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // Run after Spring Security filters
        return -1;
    }
}