package com.rc.app.api;

import com.rc.app.service.NotificationService;
import com.rc.db.tables.pojos.Notifications;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateNotificationRequest request) {
        Notifications notification = notificationService.create(request);
        Map<String, Object> response = new HashMap<>();
        response.put("id", notification.getId());
        response.put("status", "accepted");
        response.put("accepted_at", Instant.now());
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationStatusResponse> getStatus(@PathVariable("id") Long id) {
        Notifications notification = notificationService.getById(id);
        return ResponseEntity.ok(NotificationStatusResponse.from(notification));
    }
}
