package com.dormitory.service;

import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {

    public void validateIdToken(String idToken) {
        // TODO: verify Google ID token and extract email claim
        throw new UnsupportedOperationException("Google ID token validation not implemented yet.");
    }
}
