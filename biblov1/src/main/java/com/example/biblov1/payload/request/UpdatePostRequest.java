package com.example.biblov1.payload.request;

import lombok.Data;
import java.util.Set;

@Data
public class UpdatePostRequest {
    private String content;
    private String imageUrl;
    private Set<String> hashtags;
} 