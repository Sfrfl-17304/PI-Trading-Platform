package com.example.userservice.event;

import java.time.Instant;
import java.util.UUID;

public class UserRegisteredEvent {

    private String eventType;
    private UUID userId;
    private String username;
    private String email;
    private Instant timestamp;

    public UserRegisteredEvent() {}

    public UserRegisteredEvent(
        String eventType,
        UUID userId,
        String username,
        String email,
        Instant timestamp
    ) {
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
