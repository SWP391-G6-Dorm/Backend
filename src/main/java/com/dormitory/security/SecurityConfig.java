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

            // Enable CORS using a bean by default
            .cors(org.springframework.security.config.Customizer.withDefaults())

            // Stateless session — JWT carries all state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // ── Public auth endpoints ──────────────────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/google",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password"
                ).permitAll()

                // ── Public read endpoints (listings) ──────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/public/**",
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

                // Allow Spring Boot error endpoint to return actual 4xx/5xx instead of 403
                .requestMatchers("/error").permitAll()

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

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
