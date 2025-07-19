package com.example.biblov1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

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

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "profile_id"))
    private List<LanguageProficiency> languages = new ArrayList<>();

    @Embedded
    private LookingFor lookingFor = new LookingFor();

    @Embedded
    private SocialLinks socialLinks = new SocialLinks();

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
} 