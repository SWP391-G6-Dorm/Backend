package com.dormitory.dto.response;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String role;
    private boolean landlordVerified;
    /** True if LANDLORD has already submitted CCCD/identity info */
    private boolean identityInfoSubmitted;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.role = role;
        this.landlordVerified = false;
    }

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, String role, boolean landlordVerified, boolean identityInfoSubmitted) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.role = role;
        this.landlordVerified = landlordVerified;
        this.identityInfoSubmitted = identityInfoSubmitted;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isLandlordVerified() {
        return landlordVerified;
    }

    public void setLandlordVerified(boolean landlordVerified) {
        this.landlordVerified = landlordVerified;
    }

    public boolean isIdentityInfoSubmitted() {
        return identityInfoSubmitted;
    }

    public void setIdentityInfoSubmitted(boolean identityInfoSubmitted) {
        this.identityInfoSubmitted = identityInfoSubmitted;
    }
}
