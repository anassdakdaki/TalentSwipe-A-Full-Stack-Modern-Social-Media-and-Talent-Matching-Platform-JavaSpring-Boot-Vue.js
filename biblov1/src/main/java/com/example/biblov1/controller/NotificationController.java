package com.example.biblov1.controller;

import com.example.biblov1.payload.response.NotificationFeedResponse;
import com.example.biblov1.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<NotificationFeedResponse> getMyNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "25") int limit,
            @RequestParam(required = false) String since
    ) {
        LocalDateTime sinceDateTime = null;
        if (since != null && !since.isBlank()) {
            try {
                sinceDateTime = LocalDateTime.parse(since);
            } catch (DateTimeParseException ignored) {
                try {
                    sinceDateTime = OffsetDateTime.parse(since).toLocalDateTime();
                } catch (DateTimeParseException ignoredAgain) {
                    sinceDateTime = null;
                }
            }
        }

        NotificationFeedResponse response = notificationService.getNotificationFeed(userId, limit, sinceDateTime);
        return ResponseEntity.ok(response);
    }
}
