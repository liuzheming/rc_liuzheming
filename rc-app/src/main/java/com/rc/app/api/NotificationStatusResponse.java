package com.rc.app.api;

import com.rc.db.tables.pojos.Notifications;

import java.time.LocalDateTime;

public class NotificationStatusResponse {

    private Long id;
    private String status;
    private int attempts;
    private LocalDateTime nextRetryAt;
    private String lastError;

    public static NotificationStatusResponse from(Notifications notification) {
        NotificationStatusResponse response = new NotificationStatusResponse();
        response.setId(notification.getId());
        response.setStatus(notification.getStatus());
        response.setAttempts(notification.getAttempts() == null ? 0 : notification.getAttempts());
        response.setNextRetryAt(notification.getNextRetryAt());
        response.setLastError(notification.getLastError());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
