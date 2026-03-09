package com.example.biblov1.payload.response.profile;

import com.example.biblov1.model.ProfileSectionVisibility;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProfileEditorResponse {
    private Long userId;
    private String name;
    private String email;
    private Integer age;
    private String gender;
    private String university;
    private String major;
    private String location;
    private String bio;
    private String headline;
    private boolean openToCollaborate;
    private String profilePictureUrl;

    private List<String> interests = new ArrayList<>();
    private List<LanguageItem> languages = new ArrayList<>();
    private LookingForItem lookingFor = new LookingForItem();
    private SocialLinksItem socialLinks = new SocialLinksItem();
    private List<Long> featuredCommunityIds = new ArrayList<>();
    private VisibilitySettingsItem visibilitySettings = new VisibilitySettingsItem();

    private List<ExperienceItem> experiences = new ArrayList<>();
    private List<ProjectItem> projects = new ArrayList<>();
    private List<CollaborationItem> collaborations = new ArrayList<>();

    @Data
    public static class LanguageItem {
        private String name;
        private String level;
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
    public static class SocialLinksItem {
        private String github;
        private String linkedin;
        private String instagram;
    }

    @Data
    public static class VisibilitySettingsItem {
        private ProfileSectionVisibility about = ProfileSectionVisibility.PUBLIC;
        private ProfileSectionVisibility experience = ProfileSectionVisibility.PUBLIC;
        private ProfileSectionVisibility projects = ProfileSectionVisibility.PUBLIC;
        private ProfileSectionVisibility collaborations = ProfileSectionVisibility.PUBLIC;
        private ProfileSectionVisibility communities = ProfileSectionVisibility.PUBLIC;
        private ProfileSectionVisibility activity = ProfileSectionVisibility.PUBLIC;
        private ProfileSectionVisibility socialLinks = ProfileSectionVisibility.PUBLIC;
    }

    @Data
    public static class ExperienceItem {
        private Long id;
        private String title;
        private String organization;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isCurrent;
        private String description;
        private Integer sortOrder;
    }

    @Data
    public static class ProjectItem {
        private Long id;
        private String title;
        private String shortDescription;
        private String description;
        private String coverImageUrl;
        private String projectUrl;
        private String repoUrl;
        private List<String> techStack = new ArrayList<>();
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer sortOrder;
    }

    @Data
    public static class CollaborationItem {
        private Long id;
        private String title;
        private String partnerName;
        private String collaborationType;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private String resultSummary;
        private String referenceUrl;
        private Integer sortOrder;
    }
}