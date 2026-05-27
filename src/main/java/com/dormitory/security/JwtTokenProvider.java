package com.dormitory.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private String accessTokenExpiration;

    public String createAccessToken(String subject, String role) {
        // TODO: implement JWT creation using secret and expiry
        return "access-token-placeholder";
    }

    public String createRefreshToken(String subject) {
        // TODO: implement refresh token creation and storage
        return "refresh-token-placeholder";
    }

    public Date getExpirationDate() {
        return new Date(System.currentTimeMillis() + 900_000);
    }
}
