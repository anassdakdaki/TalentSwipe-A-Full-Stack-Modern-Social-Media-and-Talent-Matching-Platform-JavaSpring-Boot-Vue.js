package com.example.biblov1.service;

import com.example.biblov1.model.UserProfile;
import com.example.biblov1.model.User;
import com.example.biblov1.repository.UserProfileRepository;
import com.example.biblov1.repository.UserSwipeRepository;
import com.example.biblov1.repository.UserRepository;
import com.example.biblov1.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private final UserProfileRepository profileRepository;
    private final UserSwipeRepository userSwipeRepository;
    private final UserRepository userRepository;
    
    @Value("${app.upload.dir}")
    private String uploadDir;

    public UserProfileService(UserProfileRepository profileRepository, UserSwipeRepository userSwipeRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userSwipeRepository = userSwipeRepository;
        this.userRepository = userRepository;
    }

    public UserProfile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
    }

    public UserProfile createProfile(UserProfile profile) {
        return profileRepository.save(profile);
    }

    public UserProfile updateProfile(Long userId, UserProfile updatedProfile) {
        UserProfile existingProfile = getProfileByUserId(userId);
        
        // Update fields
        existingProfile.setName(updatedProfile.getName());
        existingProfile.setAge(updatedProfile.getAge());
        existingProfile.setGender(updatedProfile.getGender());
        existingProfile.setUniversity(updatedProfile.getUniversity());
        existingProfile.setMajor(updatedProfile.getMajor());
        existingProfile.setLocation(updatedProfile.getLocation());
        existingProfile.setBio(updatedProfile.getBio());
        existingProfile.setInterests(updatedProfile.getInterests());
        existingProfile.setLanguages(updatedProfile.getLanguages());
        existingProfile.setLookingFor(updatedProfile.getLookingFor());
        existingProfile.setSocialLinks(updatedProfile.getSocialLinks());

        return profileRepository.save(existingProfile);
    }

    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get the user's profile
        UserProfile profile = getProfileByUserId(userId);
        
        // Delete old profile picture if it exists
        if (profile.getProfilePictureUrl() != null) {
            String oldFilename = profile.getProfilePictureUrl().substring("/uploads/".length());
            Path oldFilePath = uploadPath.resolve(oldFilename);
            Files.deleteIfExists(oldFilePath);
        }

        // Generate unique filename with extension
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // Update profile with new picture URL
        String pictureUrl = "/uploads/" + filename;
        profile.setProfilePictureUrl(pictureUrl);
        profileRepository.save(profile);

        return pictureUrl;
    }

    public void deleteProfilePicture(Long userId) throws IOException {
        UserProfile profile = getProfileByUserId(userId);

        if (profile.getProfilePictureUrl() != null) {
            String oldFilename = profile.getProfilePictureUrl().substring("/uploads/".length());
            Path uploadPath = Paths.get(uploadDir);
            Path oldFilePath = uploadPath.resolve(oldFilename);
            
            // Delete the file
            Files.deleteIfExists(oldFilePath);

            // Clear the URL in the profile and save
            profile.setProfilePictureUrl(null);
            profileRepository.save(profile);
        }
        // If profilePictureUrl is null, do nothing
    }

    public List<UserProfile> getAllProfilesExcludingUser(Long userId) {
        User currentUser = userRepository.findById(userId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Get IDs of profiles the current user has already swiped on
        Set<Long> swipedUserIds = userSwipeRepository.findBySwiper(currentUser).stream()
                                                    .map(userSwipe -> userSwipe.getSwiped().getId())
                                                    .collect(Collectors.toSet());

        // Also exclude the current user's own profile
        swipedUserIds.add(userId);

        // Fetch all profiles, then filter out swiped ones
        return profileRepository.findAll().stream()
                                .filter(profile -> !swipedUserIds.contains(profile.getUser().getId()))
                                .collect(Collectors.toList());
    }
}