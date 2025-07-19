package com.example.biblov1.payload.request;

import com.example.biblov1.model.UserSwipe.SwipeType;

public class SwipeRequest {
    private Long swipedUserId;
    private SwipeType swipeType;

    public Long getSwipedUserId() {
        return swipedUserId;
    }

    public void setSwipedUserId(Long swipedUserId) {
        this.swipedUserId = swipedUserId;
    }

    public SwipeType getSwipeType() {
        return swipeType;
    }

    public void setSwipeType(SwipeType swipeType) {
        this.swipeType = swipeType;
    }
} 