package com.example.biblov1.payload.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateCommunityRequest {
    private String name;
    private String description;
    private List<String> tags;
} 