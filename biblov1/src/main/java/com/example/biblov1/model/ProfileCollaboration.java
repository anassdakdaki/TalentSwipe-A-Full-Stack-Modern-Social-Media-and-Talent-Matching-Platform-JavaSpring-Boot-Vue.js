package com.example.biblov1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "profile_collaborations")
public class ProfileCollaboration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonIgnore
    private UserProfile profile;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 120)
    private String partnerName;

    @Column(length = 120)
    private String collaborationType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String resultSummary;

    @Column(length = 1024)
    private String referenceUrl;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}