package com.example.biblov1.service.events;

public record PostCreatedEvent(
        Long postId,
        Long authorId,
        Long communityId
) {
}
