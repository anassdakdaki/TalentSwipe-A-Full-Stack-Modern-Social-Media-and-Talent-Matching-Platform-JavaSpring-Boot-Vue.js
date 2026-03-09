package com.example.biblov1.controller;

import com.example.biblov1.payload.request.profile.UpdateProfileEditorRequest;
import com.example.biblov1.payload.response.profile.ProfileEditorResponse;
import com.example.biblov1.payload.response.profile.ProfilePublicResponse;
import com.example.biblov1.payload.response.profile.ProfileSummaryResponse;
import com.example.biblov1.service.ProfileEditorService;
import com.example.biblov1.service.ProfilePublicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final ProfileEditorService profileEditorService;
    private final ProfilePublicService profilePublicService;

    public ProfileController(ProfileEditorService profileEditorService, ProfilePublicService profilePublicService) {
        this.profileEditorService = profileEditorService;
        this.profilePublicService = profilePublicService;
    }

    @GetMapping("/me/editor")
    public ResponseEntity<ProfileEditorResponse> getEditorProfile(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(profileEditorService.loadEditorProfile(userId));
    }

    @PutMapping("/me/editor")
    public ResponseEntity<?> updateEditorProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateProfileEditorRequest request
    ) {
        try {
            return ResponseEntity.ok(profileEditorService.updateEditorProfile(userId, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{userId}/public")
    public ResponseEntity<?> getPublicProfile(
            @PathVariable("userId") Long targetUserId,
            @RequestAttribute("userId") Long viewerUserId
    ) {
        try {
            ProfilePublicResponse response = profilePublicService.getPublicProfile(targetUserId, viewerUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<?> getProfileSummary(
            @PathVariable("userId") Long targetUserId,
            @RequestAttribute("userId") Long viewerUserId
    ) {
        try {
            ProfileSummaryResponse response = profilePublicService.getProfileSummary(targetUserId, viewerUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }
}
