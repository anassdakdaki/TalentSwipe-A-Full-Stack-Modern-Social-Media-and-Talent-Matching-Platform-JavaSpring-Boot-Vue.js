package com.example.biblov1.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long authorId;
    private String authorName;
    private String authorProfilePictureUrl;
    private Long communityId;
    private String communityName;
    private String communityImageUrl;
    private Set<String> hashtags;
    private long likesCount;
    @JsonProperty("isLiked")
    private boolean isLiked;
    private long commentsCount;
}
