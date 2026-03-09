package com.example.biblov1.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AppNotificationResponse {
    private String id;
    private String type;
    private String title;
    private String message;
    private String actorName;
    private Long relatedId;
    private String route;
    private LocalDateTime createdAt;
}
