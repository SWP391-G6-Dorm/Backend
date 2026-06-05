package com.dormitory.dto.request;

import com.dormitory.entity.User;
import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank
    private String idToken;

    /**
     * Role selected by the user on the Register page (TENANT or LANDLORD).
     * Used only when creating a new account via Google OAuth.
     * Defaults to TENANT if not provided.
     */
    private User.Role role = User.Role.TENANT;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String idToken, User.Role role) {
        this.idToken = idToken;
        this.role = role;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }
}
