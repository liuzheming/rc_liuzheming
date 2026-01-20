package com.rc.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.app.api.CreateNotificationRequest;
import com.rc.core.model.NotificationStatus;
import com.rc.db.tables.pojos.Notifications;
import com.rc.repo.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Notifications create(CreateNotificationRequest request) {
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isEmpty()) {
            Optional<Notifications> existing = repository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Notifications notification = new Notifications()
                .setTargetUrl(request.getTargetUrl())
                .setMethod(request.getMethod().toUpperCase())
                .setHeaders(serializeHeaders(request))
                .setBody(request.getBody())
                .setStatus(NotificationStatus.PENDING.name())
                .setAttempts(0)
                .setNextRetryAt(now)
                .setLastError(null)
                .setIdempotencyKey(request.getIdempotencyKey())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        return repository.insert(notification);
    }

    public Notifications getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notification not found"));
    }

    private String serializeHeaders(CreateNotificationRequest request) {
        if (request.getHeaders() == null || request.getHeaders().isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(request.getHeaders());
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
