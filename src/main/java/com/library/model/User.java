package com.library.model;

import java.time.LocalDateTime;

public class User {
    private Integer id;
    private String email;
    private String passwordHash;
    private String role;
    private LocalDateTime createdAt;
    private String tempPassword;
    private LocalDateTime tempPasswordExpiry;
    private boolean passwordResetRequired;

    // Constructor
    public User() {
    }

    public User(Integer id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }

    public LocalDateTime getTempPasswordExpiry() {
        return tempPasswordExpiry;
    }

    public void setTempPasswordExpiry(LocalDateTime tempPasswordExpiry) {
        this.tempPasswordExpiry = tempPasswordExpiry;
    }

    public boolean isPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }
}