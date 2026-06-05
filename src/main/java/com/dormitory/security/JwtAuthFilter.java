package com.dormitory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

/**
 * Reads the Bearer JWT from the Authorization header,
 * validates it, and sets the Authentication in the SecurityContext.
 *
 * Must be registered BEFORE UsernamePasswordAuthenticationFilter
 * in SecurityConfig.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:default-secret-key-for-development-only-change-in-production-1234567890}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Key signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String subject = claims.getSubject();
                String role    = claims.get("role", String.class);
                Boolean landlordVerified = claims.get("landlordVerified", Boolean.class);

                if (StringUtils.hasText(subject) && StringUtils.hasText(role)) {
                    java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(role));

                    // Verified landlords get extra authority for sensitive endpoints
                    // Controllers use: @PreAuthorize("hasAuthority('LANDLORD_VERIFIED')")
                    if (Boolean.TRUE.equals(landlordVerified)) {
                        authorities.add(new SimpleGrantedAuthority("LANDLORD_VERIFIED"));
                    }

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    subject,
                                    null,
                                    authorities
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Invalid token — leave SecurityContext empty; 401/403 will be returned
                logger.warn("Invalid JWT token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Extracts the raw token string from "Authorization: Bearer <token>" */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
