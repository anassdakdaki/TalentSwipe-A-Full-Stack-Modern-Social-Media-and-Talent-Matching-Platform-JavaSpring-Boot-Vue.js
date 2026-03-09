package com.example.biblov1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "profile_projects")
public class ProfileProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonIgnore
    private UserProfile profile;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 220)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1024)
    private String coverImageUrl;

    @Column(length = 1024)
    private String projectUrl;

    @Column(length = 1024)
    private String repoUrl;

    @ElementCollection
    @CollectionTable(name = "profile_project_tech_stack", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tech")
    private List<String> techStack = new ArrayList<>();

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}