package com.example.biblov1.payload.response.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityItem {
    private ActivityType type;
    private String title;
    private LocalDateTime timestamp;
    private Long referenceId;
    private String referenceRoute;

    public enum ActivityType {
        POST,
        COMMENT,
        COMMUNITY_JOIN,
        COMMUNITY_CREATE
    }
}