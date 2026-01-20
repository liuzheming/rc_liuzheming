CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_url VARCHAR(2048) NOT NULL,
    method VARCHAR(16) NOT NULL,
    headers TEXT,
    body TEXT,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL,
    next_retry_at TIMESTAMP NULL,
    last_error TEXT,
    idempotency_key VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_status_retry
    ON notifications (status, next_retry_at);

CREATE INDEX idx_notifications_created_at
    ON notifications (created_at);

CREATE INDEX idx_notifications_idempotency_key
    ON notifications (idempotency_key);
