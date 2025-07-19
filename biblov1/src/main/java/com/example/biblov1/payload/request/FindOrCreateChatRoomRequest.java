package com.example.biblov1.payload.request;

import lombok.Data;

@Data
public class FindOrCreateChatRoomRequest {
    private Long user1Id;
    private Long user2Id;
    private Long studyMatchId;
} 