package com.example.biblov1.payload.request.onboarding;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompleteOnboardingRequest {
    private List<String> interests = new ArrayList<>();
    private List<Long> selectedCommunityIds = new ArrayList<>();
    private List<String> preferredCommunityTags = new ArrayList<>();
    private LookingForItem lookingFor = new LookingForItem();
    private ContentPreferencesItem contentPreferences = new ContentPreferencesItem();
    private Boolean openToCollaborate;

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
}