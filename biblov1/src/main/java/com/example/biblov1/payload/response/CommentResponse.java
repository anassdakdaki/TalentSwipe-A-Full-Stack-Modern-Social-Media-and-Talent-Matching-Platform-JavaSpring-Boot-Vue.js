package com.example.biblov1.payload.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName; // To display author's name directly
    private String authorProfilePictureUrl;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse(
            Long id,
            String content,
            Long authorId,
            String authorName,
            String authorProfilePictureUrl,
            Long postId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorProfilePictureUrl = authorProfilePictureUrl;
        this.postId = postId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
