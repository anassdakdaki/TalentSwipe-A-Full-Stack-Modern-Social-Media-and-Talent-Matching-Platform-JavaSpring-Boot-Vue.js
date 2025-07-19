package com.example.biblov1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_swipes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"swiper_id", "swiped_id"})
})
public class UserSwipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiper_id", nullable = false)
    private User swiper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiped_id", nullable = false)
    private User swiped;

    @Enumerated(EnumType.STRING)
    @Column(name = "swipe_type", nullable = false)
    private SwipeType swipeType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SwipeType {
        LIKE,
        DISLIKE
    }

    // Getters and setters are provided by Lombok's @Data annotation
} 