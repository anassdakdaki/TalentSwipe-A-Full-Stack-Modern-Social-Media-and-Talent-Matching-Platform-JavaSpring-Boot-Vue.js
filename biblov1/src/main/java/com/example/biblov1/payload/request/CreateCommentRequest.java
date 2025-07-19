package com.example.biblov1.payload.request;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private Long postId;
    private String content;
} 