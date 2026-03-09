package com.example.biblov1.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class NotificationFeedResponse {
    private List<AppNotificationResponse> notifications;
    private long unreadCount;
    private Map<String, Long> countsByType;
}
