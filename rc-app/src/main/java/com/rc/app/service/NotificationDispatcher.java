package com.rc.app.service;

import com.rc.core.model.NotificationStatus;
import com.rc.db.tables.pojos.Notifications;
import com.rc.repo.NotificationRepository;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NotificationDispatcher {

    private final NotificationRepository repository;
    private final RetryTemplate retryTemplate;

    private final int maxAttempts = 8;
    private final long baseDelaySeconds = 60;
    private final long maxDelaySeconds = 86400;

    public NotificationDispatcher(NotificationRepository repository,
                                  RetryTemplate retryTemplate) {
        this.repository = repository;
        this.retryTemplate = retryTemplate;
    }

    @Scheduled(fixedDelayString = "${notification.dispatcher.interval-ms:2000}")
    public void dispatchDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Notifications> due = repository.findDueNotifications(now, 100);
        for (Notifications notification : due) {
            dispatch(notification);
        }
    }

    public void dispatch(Notifications notification) {
        notification.setStatus(NotificationStatus.SENDING.name());
        notification.setUpdatedAt(LocalDateTime.now());
        repository.update(notification);

        try {
            retryTemplate.execute(context -> {
                send(notification);
                return null;
            });
            markSuccess(notification);
        } catch (Exception ex) {
            markFailed(notification, ex);
        }
    }

    private void send(Notifications notification) {
        // Mock success for MVP verification.
        return;
    }

    private void markSuccess(Notifications notification) {
        notification.setStatus(NotificationStatus.SUCCEEDED.name());
        notification.setLastError(null);
        notification.setUpdatedAt(LocalDateTime.now());
        repository.update(notification);
    }

    private void markFailed(Notifications notification, Exception ex) {
        int attempts = (notification.getAttempts() == null ? 0 : notification.getAttempts()) + 1;
        notification.setAttempts(attempts);
        notification.setLastError(ex.getMessage());
        notification.setUpdatedAt(LocalDateTime.now());

        if (attempts >= maxAttempts) {
            notification.setStatus(NotificationStatus.DEAD.name());
            notification.setNextRetryAt(null);
        } else {
            notification.setStatus(NotificationStatus.FAILED.name());
            notification.setNextRetryAt(nextRetryTime(attempts));
        }
        repository.update(notification);
    }

    private LocalDateTime nextRetryTime(int attempts) {
        long delaySeconds = (long) (baseDelaySeconds * Math.pow(2, Math.max(0, attempts - 1)));
        delaySeconds = Math.min(delaySeconds, maxDelaySeconds);
        double jitter = 0.9 + ThreadLocalRandom.current().nextDouble(0.2);
        delaySeconds = (long) (delaySeconds * jitter);
        return LocalDateTime.now().plus(Duration.ofSeconds(delaySeconds));
    }
}
