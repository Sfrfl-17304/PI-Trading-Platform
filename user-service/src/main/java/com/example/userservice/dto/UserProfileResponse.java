package com.example.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private BigDecimal balance;
    private Instant createdAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(UUID id, String username, String email, BigDecimal balance, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
