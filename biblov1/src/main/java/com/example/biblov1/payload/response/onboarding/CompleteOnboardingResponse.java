package com.example.biblov1.payload.response.onboarding;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompleteOnboardingResponse {
    private boolean completed;
    private int joinedCommunitiesCount;
    private List<Long> joinedCommunityIds = new ArrayList<>();
}