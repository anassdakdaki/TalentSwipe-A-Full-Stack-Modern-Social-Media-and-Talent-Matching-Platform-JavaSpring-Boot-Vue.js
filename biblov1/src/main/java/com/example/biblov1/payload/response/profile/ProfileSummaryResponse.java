package com.example.biblov1.payload.response.profile;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProfileSummaryResponse {
    private Long userId;
    private String name;
    private String profilePictureUrl;
    private String headline;
    private String location;
    private String university;
    private String major;
    private String bio;
    private List<String> topInterests = new ArrayList<>();
    private ProfileStatsResponse stats;
    private boolean isSelf;
    private boolean isConnected;
    private boolean canMessage;
}