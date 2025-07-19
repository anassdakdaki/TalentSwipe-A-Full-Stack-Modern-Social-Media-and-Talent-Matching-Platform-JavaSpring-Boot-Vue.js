package com.example.biblov1.payload.response;

import com.example.biblov1.model.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName; // To display author's name directly
    private Long authorId;     // Add authorId field
    private Long communityId;
    private Set<String> hashtags;
    private long likesCount;
    private boolean isLiked;
    private List<CommentResponse> comments; // Assuming a CommentResponse DTO will be created later

    // Constructor for creating from Post entity and additional data
    public PostResponse(Long id, String content, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt, String authorName, Long communityId, Set<String> hashtags, long likesCount, boolean isLiked, List<CommentResponse> comments) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorName = authorName;
        this.communityId = communityId;
        this.hashtags = hashtags;
        this.likesCount = likesCount;
        this.isLiked = isLiked;
        this.comments = comments;
    }

    // Constructor including authorId
    public PostResponse(Long id, String content, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt, String authorName, Long authorId, Long communityId, Set<String> hashtags, long likesCount, boolean isLiked, List<CommentResponse> comments) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorName = authorName;
        this.authorId = authorId; // Initialize authorId
        this.communityId = communityId;
        this.hashtags = hashtags;
        this.likesCount = likesCount;
        this.isLiked = isLiked;
        this.comments = comments;
    }
} 