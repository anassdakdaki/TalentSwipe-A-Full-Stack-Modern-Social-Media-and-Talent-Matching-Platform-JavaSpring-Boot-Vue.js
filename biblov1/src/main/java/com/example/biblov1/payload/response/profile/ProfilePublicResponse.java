package com.example.biblov1.payload.response.profile;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProfilePublicResponse {
    private Long userId;
    private String name;
    private String profilePictureUrl;
    private String headline;
    private Integer age;
    private String gender;
    private String location;
    private String university;
    private String major;
    private boolean openToCollaborate;

    private String bio;
    private LookingForItem lookingFor = new LookingForItem();
    private List<String> interests = new ArrayList<>();
    private List<LanguageItem> languages = new ArrayList<>();
    private SocialLinksItem socialLinks = new SocialLinksItem();

    private List<ExperienceItem> experiences = new ArrayList<>();
    private List<ProjectItem> projects = new ArrayList<>();
    private List<CollaborationItem> collaborations = new ArrayList<>();
    private List<CommunityItem> communities = new ArrayList<>();
    private ProfileStatsResponse stats;
    private List<RecentActivityItem> recentActivity = new ArrayList<>();

    private boolean isOwner;
    private boolean isConnected;
    private boolean canMessage;

    @Data
    public static class LookingForItem {
        private boolean studyPartner;
        private boolean languageExchange;
        private boolean friendship;
        private boolean networking;
        private boolean community;
    }

    @Data
    public static class LanguageItem {
        private String name;
        private String level;
    }

    @Data
    public static class SocialLinksItem {
        private String github;
        private String linkedin;
        private String instagram;
    }

    @Data
    public static class ExperienceItem {
        private String title;
        private String organization;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isCurrent;
        private String description;
    }

    @Data
    public static class ProjectItem {
        private String title;
        private String shortDescription;
        private String description;
        private String coverImageUrl;
        private String projectUrl;
        private String repoUrl;
        private List<String> techStack = new ArrayList<>();
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class CollaborationItem {
        private String title;
        private String partnerName;
        private String collaborationType;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private String resultSummary;
        private String referenceUrl;
    }

    @Data
    public static class CommunityItem {
        private Long id;
        private String name;
        private String role;
        private LocalDateTime timestamp;
    }
}