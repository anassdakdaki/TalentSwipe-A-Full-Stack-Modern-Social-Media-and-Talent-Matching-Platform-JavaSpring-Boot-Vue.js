package com.example.biblov1.controller;

import com.example.biblov1.model.UserProfile;
import com.example.biblov1.service.UserProfileService;
import com.example.biblov1.service.UserService;
import com.example.biblov1.model.User;
import com.example.biblov1.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "http://localhost:5173")
public class UserProfileController {
    private final UserProfileService profileService;
    private final UserService userService;

    public UserProfileController(UserProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile(@RequestAttribute("userId") Long userId) {
        try {
            return ResponseEntity.ok(profileService.getProfileByUserId(userId));
        } catch (Exception e) {
            // If profile doesn't exist, create one
            User user = userService.getUserById(userId);
            UserProfile newProfile = new UserProfile();
            newProfile.setUser(user);
            newProfile.setName(user.getName());
            newProfile.setEmail(user.getEmail());
            newProfile.setMajor("");
            newProfile.setLocation("");
            newProfile.setBio("");
            return ResponseEntity.ok(profileService.createProfile(newProfile));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    // Endpoint for updating profile without picture
    @PutMapping(value = "/me", consumes = {"application/json"})
    public ResponseEntity<UserProfile> updateMyProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody UserProfile updatedProfile) {
        return ResponseEntity.ok(profileService.updateProfile(userId, updatedProfile));
    }

    // Endpoint for updating profile with picture
    @PutMapping(value = "/me/with-picture", consumes = {"multipart/form-data"})
    public ResponseEntity<UserProfile> updateMyProfileWithPicture(
            @RequestAttribute("userId") Long userId,
            @RequestPart("profile") UserProfile updatedProfile,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            UserProfile profile = profileService.updateProfile(userId, updatedProfile);
            
            // If a new picture was uploaded, update it
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String pictureUrl = profileService.uploadProfilePicture(userId, profilePicture);
                profile.setProfilePictureUrl(pictureUrl);
                profile = profileService.createProfile(profile);
            }
            
            return ResponseEntity.ok(profile);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Keep the separate picture upload endpoint for backward compatibility
    @PostMapping("/me/picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestAttribute("userId") Long userId,
            @RequestParam("profilePicture") MultipartFile file) {
        try {
            String pictureUrl = profileService.uploadProfilePicture(userId, file);
            return ResponseEntity.ok(Map.of("profilePictureUrl", pictureUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to upload profile picture"));
        }
    }

    // Endpoint to remove profile picture
    @DeleteMapping("/me/picture")
    public ResponseEntity<?> deleteMyProfilePicture(@RequestAttribute("userId") Long userId) {
        try {
            profileService.deleteProfilePicture(userId);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete profile picture"));
        } catch (ResourceNotFoundException e) {
             return ResponseEntity.notFound().build();
        }
    }

    // Endpoint to get all profiles excluding the current user
    @GetMapping("/discover")
    public ResponseEntity<List<UserProfile>> getAllProfilesForDiscovery(@RequestAttribute("userId") Long userId) {
        List<UserProfile> profiles = profileService.getAllProfilesExcludingUser(userId);
        return ResponseEntity.ok(profiles);
    }
} 