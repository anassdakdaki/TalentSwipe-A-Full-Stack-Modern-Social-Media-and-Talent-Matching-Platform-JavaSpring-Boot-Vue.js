package com.example.biblov1.payload.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long chatRoomId;
    private String content;
} 