package com.dormitory.service;

import com.dormitory.dto.AuthResponse;
import com.dormitory.dto.GoogleAuthRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        throw new UnsupportedOperationException("Google authentication flow is not implemented yet.");
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        throw new UnsupportedOperationException("Refresh token flow is not implemented yet.");
    }
}
