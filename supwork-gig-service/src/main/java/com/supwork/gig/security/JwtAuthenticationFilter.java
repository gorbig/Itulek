package com.supwork.gig.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        log.info("🔍 Request: {} {}", request.getMethod(), request.getRequestURI());
        
        String authHeader = request.getHeader("Authorization");
        
        // Log the auth header (first 30 chars to not expose full token)
        log.info("📝 Auth header: {}", authHeader != null && authHeader.length() > 30 
            ? authHeader.substring(0, 30) + "..." 
            : authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                
                // Log token format (first 20 chars)
                String tokenPreview = token.length() > 20 ? token.substring(0, 20) + "..." : token;
                log.debug("Token preview: {}", tokenPreview);
                
                // Check token format (should have 2 periods for JWT structure: header.payload.signature)
                if (!token.contains(".")) {
                    log.warn("Invalid JWT format: token does not contain '.' separator");
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Split and check token parts
                String[] parts = token.split("\\.");
                if (parts.length != 3) {
                    log.warn("Invalid JWT format: expected 3 parts, found {}", parts.length);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                String userId = claims.getSubject();
                // Try "roles" first (as used by user-service), fallback to "role"
                String role = claims.get("roles", String.class);
                if (role == null) {
                    role = claims.get("role", String.class);
                }
                
                log.info("✅ JWT validated successfully. UserId: {}, Role: {}", userId, role);
                
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );
                    
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("✅ Authentication set for user: {}", userId);
                }
            } catch (Exception e) {
                log.error("❌ JWT validation failed: {}", e.getMessage());
                e.printStackTrace();
                SecurityContextHolder.clearContext();
            }
        } else {
            log.warn("⚠️  No Authorization header or invalid format");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
