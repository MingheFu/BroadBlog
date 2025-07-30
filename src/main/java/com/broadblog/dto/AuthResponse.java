package com.broadblog.dto;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDTO user;
    private String message;

    // Default constructor
    public AuthResponse() {}

    // Constructor with all fields
    public AuthResponse(String token, String refreshToken, UserDTO user, String message) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
        this.message = message;
    }

    // Constructor for success response
    public AuthResponse(String token, String refreshToken, UserDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
        this.message = "Authentication successful";
    }

    // Constructor for error response
    public AuthResponse(String message) {
        this.message = message;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}