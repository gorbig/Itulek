package com.supwork.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Disabled: Gateway no longer validates JWT, services handle their own authentication
// @Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip JWT validation for actuator endpoints
        if (request.getPath().value().startsWith("/actuator")) {
            return chain.filter(exchange);
        }
        
        // Mock JWT validation
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // TODO: Implement real JWT validation
            if (isValidToken(token)) {
                return chain.filter(exchange);
            }
        }
        
        // For now, allow all requests (mock validation)
        // In production, uncomment below to enforce authentication
        // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // return exchange.getResponse().setComplete();
        
        return chain.filter(exchange);
    }

    private boolean isValidToken(String token) {
        // Mock validation - always return true
        // TODO: Implement real JWT validation logic
        return true;
    }

    @Override
    public int getOrder() {
        return -100;
    }

}

