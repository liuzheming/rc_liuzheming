package com.rc.repo;

import com.rc.core.model.NotificationStatus;
import com.rc.db.tables.pojos.Notifications;
import com.rc.db.tables.records.NotificationsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.rc.db.tables.Notifications.NOTIFICATIONS;

@Repository
public class NotificationRepository {

    private final DSLContext dsl;

    public NotificationRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Notifications> findDueNotifications(LocalDateTime now, int limit) {
        return dsl.selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.STATUS.in(NotificationStatus.PENDING.name(), NotificationStatus.FAILED.name()))
                .and(NOTIFICATIONS.NEXT_RETRY_AT.le(now))
                .orderBy(NOTIFICATIONS.NEXT_RETRY_AT.asc())
                .limit(limit)
                .fetchInto(Notifications.class);
    }

    public Optional<Notifications> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return Optional.empty();
        }
        Notifications notification = dsl.selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.IDEMPOTENCY_KEY.eq(idempotencyKey))
                .fetchOneInto(Notifications.class);
        return Optional.ofNullable(notification);
    }

    public Optional<Notifications> findById(Long id) {
        Notifications notification = dsl.selectFrom(NOTIFICATIONS)
                .where(NOTIFICATIONS.ID.eq(id))
                .fetchOneInto(Notifications.class);
        return Optional.ofNullable(notification);
    }

    public Notifications insert(Notifications notification) {
        NotificationsRecord record = dsl.insertInto(NOTIFICATIONS)
                .set(NOTIFICATIONS.TARGET_URL, notification.getTargetUrl())
                .set(NOTIFICATIONS.METHOD, notification.getMethod())
                .set(NOTIFICATIONS.HEADERS, notification.getHeaders())
                .set(NOTIFICATIONS.BODY, notification.getBody())
                .set(NOTIFICATIONS.STATUS, notification.getStatus())
                .set(NOTIFICATIONS.ATTEMPTS, notification.getAttempts())
                .set(NOTIFICATIONS.NEXT_RETRY_AT, notification.getNextRetryAt())
                .set(NOTIFICATIONS.LAST_ERROR, notification.getLastError())
                .set(NOTIFICATIONS.IDEMPOTENCY_KEY, notification.getIdempotencyKey())
                .set(NOTIFICATIONS.CREATED_AT, notification.getCreatedAt())
                .set(NOTIFICATIONS.UPDATED_AT, notification.getUpdatedAt())
                .returning()
                .fetchOne();
        return record == null ? notification : record.into(Notifications.class);
    }

    public void update(Notifications notification) {
        dsl.update(NOTIFICATIONS)
                .set(NOTIFICATIONS.TARGET_URL, notification.getTargetUrl())
                .set(NOTIFICATIONS.METHOD, notification.getMethod())
                .set(NOTIFICATIONS.HEADERS, notification.getHeaders())
                .set(NOTIFICATIONS.BODY, notification.getBody())
                .set(NOTIFICATIONS.STATUS, notification.getStatus())
                .set(NOTIFICATIONS.ATTEMPTS, notification.getAttempts())
                .set(NOTIFICATIONS.NEXT_RETRY_AT, notification.getNextRetryAt())
                .set(NOTIFICATIONS.LAST_ERROR, notification.getLastError())
                .set(NOTIFICATIONS.IDEMPOTENCY_KEY, notification.getIdempotencyKey())
                .set(NOTIFICATIONS.UPDATED_AT, notification.getUpdatedAt())
                .where(NOTIFICATIONS.ID.eq(notification.getId()))
                .execute();
    }
}
