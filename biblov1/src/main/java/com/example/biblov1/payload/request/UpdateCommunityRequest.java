package com.example.biblov1.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCommunityRequest {
    private String name;
    private String description;
    private List<String> tags;
    private Boolean removeImage;
}
