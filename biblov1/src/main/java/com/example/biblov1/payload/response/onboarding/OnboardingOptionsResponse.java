package com.example.biblov1.payload.response.onboarding;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OnboardingOptionsResponse {
    private boolean completed;
    private List<String> interestSuggestions = new ArrayList<>();
    private List<String> communityTagSuggestions = new ArrayList<>();
    private List<String> currentInterests = new ArrayList<>();
    private List<String> currentPreferredCommunityTags = new ArrayList<>();
    private List<Long> joinedCommunityIds = new ArrayList<>();
    private List<CommunityOptionItem> communityOptions = new ArrayList<>();
    private LookingForItem currentLookingFor = new LookingForItem();
    private ContentPreferencesItem currentContentPreferences = new ContentPreferencesItem();
    private boolean openToCollaborate;
    private List<ContentOptionItem> contentOptions = new ArrayList<>();

    @Data
    public static class CommunityOptionItem {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private List<String> tags = new ArrayList<>();
        private long membersCount;
        private boolean joined;
        private boolean recommended;
        private String recommendationReason;
    }

    @Data
    public static class LookingForItem {
        private boolean studyPartner;
        private boolean languageExchange;
        private boolean friendship;
        private boolean networking;
        private boolean community;
    }

    @Data
    public static class ContentPreferencesItem {
        private boolean studyGuides = true;
        private boolean projectShowcases = true;
        private boolean communityDiscussions = true;
        private boolean events = true;
        private boolean careerOpportunities = true;
    }

    @Data
    public static class ContentOptionItem {
        private String key;
        private String label;
        private String description;
    }
}