package com.example.biblov1.payload.response.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileStatsResponse {
    private long postsCount;
    private long commentsCount;
    private long communitiesJoinedCount;
    private long communitiesOwnedCount;
    private long matchesCount;
}