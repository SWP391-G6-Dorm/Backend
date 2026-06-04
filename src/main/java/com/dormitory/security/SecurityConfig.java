package com.dormitory.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Core Spring Security configuration.
 *
 * OAuth2 auto-configuration is excluded in application.yml so this class
 * handles all security rules without requiring GOOGLE_CLIENT_ID at startup.
 * When Google OAuth2 credentials are available, add the oauth2Login() config
 * back here and remove the exclusion from application.yml.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize / @Secured on service methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — REST API with stateless JWT does not need it
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — JWT carries all state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // ── Public auth endpoints ──────────────────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password"
                ).permitAll()

                // ── Public read endpoints (listings) ──────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/properties",
                    "/api/properties/**",
                    "/api/rooms",
                    "/api/rooms/**"
                ).permitAll()

                // ── VNPay payment callback (server-to-server, no JWT) ──────
                .requestMatchers("/api/payments/vnpay/callback").permitAll()

                // ── Swagger / OpenAPI docs ─────────────────────────────────
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // ── Everything else requires authentication ────────────────
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * BCrypt password encoder (cost factor 12 per NFR-SEC-01).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
