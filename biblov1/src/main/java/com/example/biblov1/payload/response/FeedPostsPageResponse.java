package com.example.biblov1.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostsPageResponse {
    private List<FeedPostResponse> items;
    private LocalDateTime nextCursorCreatedAt;
    private Long nextCursorPostId;
    private boolean hasMore;
}

