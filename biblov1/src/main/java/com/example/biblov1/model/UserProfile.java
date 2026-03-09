package com.example.biblov1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private Integer age;

    @Column
    private String gender;

    @Column
    private String university;

    @Column(nullable = false)
    private String major;

    @Column
    private String location;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 120)
    private String headline;

    @Column(nullable = false)
    private boolean openToCollaborate = false;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;

    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "profile_id"))
    private List<LanguageProficiency> languages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_featured_communities", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "community_id")
    private List<Long> featuredCommunityIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_preferred_community_tags", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "tag")
    private List<String> preferredCommunityTags = new ArrayList<>();

    @Embedded
    private LookingFor lookingFor = new LookingFor();

    @Embedded
    private SocialLinks socialLinks = new SocialLinks();

    @Embedded
    private ContentPreferences contentPreferences = new ContentPreferences();

    @Embedded
    private VisibilitySettings visibilitySettings = new VisibilitySettings();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProfileExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProfileProject> projects = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProfileCollaboration> collaborations = new ArrayList<>();

    @Data
    @Embeddable
    public static class LanguageProficiency {
        @Column(name = "language_name")
        private String name;

        @Column(name = "proficiency_level")
        private String level;
    }

    @Data
    @Embeddable
    public static class LookingFor {
        @Column(name = "study_partner")
        private boolean studyPartner;

        @Column(name = "language_exchange")
        private boolean languageExchange;

        @Column(name = "friendship")
        private boolean friendship;

        @Column(name = "networking")
        private boolean networking;

        @Column(name = "community")
        private boolean community;
    }

    @Data
    @Embeddable
    public static class SocialLinks {
        @Column(name = "github_url")
        private String github;

        @Column(name = "linkedin_url")
        private String linkedin;

        @Column(name = "instagram_url")
        private String instagram;
    }

    @Data
    @Embeddable
    public static class ContentPreferences {
        @Column(name = "content_study_guides")
        private boolean studyGuides = true;

        @Column(name = "content_project_showcases")
        private boolean projectShowcases = true;

        @Column(name = "content_community_discussions")
        private boolean communityDiscussions = true;

        @Column(name = "content_events")
        private boolean events = true;

        @Column(name = "content_career_opportunities")
        private boolean careerOpportunities = true;
    }

    @Data
    @Embeddable
    public static class VisibilitySettings {
        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_about")
        private ProfileSectionVisibility about = ProfileSectionVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_experience")
        private ProfileSectionVisibility experience = ProfileSectionVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_projects")
        private ProfileSectionVisibility projects = ProfileSectionVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_collaborations")
        private ProfileSectionVisibility collaborations = ProfileSectionVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_communities")
        private ProfileSectionVisibility communities = ProfileSectionVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_activity")
        private ProfileSectionVisibility activity = ProfileSectionVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility_social_links")
        private ProfileSectionVisibility socialLinks = ProfileSectionVisibility.PUBLIC;
    }

    @PrePersist
    @PreUpdate
    private void ensureNestedDefaults() {
        if (lookingFor == null) {
            lookingFor = new LookingFor();
        }
        if (socialLinks == null) {
            socialLinks = new SocialLinks();
        }
        if (contentPreferences == null) {
            contentPreferences = new ContentPreferences();
        }
        if (visibilitySettings == null) {
            visibilitySettings = new VisibilitySettings();
        }
        if (featuredCommunityIds == null) {
            featuredCommunityIds = new ArrayList<>();
        }
        if (preferredCommunityTags == null) {
            preferredCommunityTags = new ArrayList<>();
        }
        if (interests == null) {
            interests = new ArrayList<>();
        }
        if (languages == null) {
            languages = new ArrayList<>();
        }
        if (experiences == null) {
            experiences = new ArrayList<>();
        }
        if (projects == null) {
            projects = new ArrayList<>();
        }
        if (collaborations == null) {
            collaborations = new ArrayList<>();
        }

        experiences.forEach(item -> item.setProfile(this));
        projects.forEach(item -> item.setProfile(this));
        collaborations.forEach(item -> item.setProfile(this));
    }

    public void setExperiences(List<ProfileExperience> experiences) {
        this.experiences = experiences == null ? new ArrayList<>() : experiences;
        this.experiences.forEach(item -> item.setProfile(this));
    }

    public void setProjects(List<ProfileProject> projects) {
        this.projects = projects == null ? new ArrayList<>() : projects;
        this.projects.forEach(item -> item.setProfile(this));
    }

    public void setCollaborations(List<ProfileCollaboration> collaborations) {
        this.collaborations = collaborations == null ? new ArrayList<>() : collaborations;
        this.collaborations.forEach(item -> item.setProfile(this));
    }
}
