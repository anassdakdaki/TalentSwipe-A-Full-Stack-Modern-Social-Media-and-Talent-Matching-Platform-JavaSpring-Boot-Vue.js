package com.example.biblov1.controller;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.User;
import com.example.biblov1.service.CommunityService;
import com.example.biblov1.service.FileStorageService;
import com.example.biblov1.service.UserService;
import com.example.biblov1.payload.request.CreateCommunityRequest;
import com.example.biblov1.payload.request.UpdateCommunityRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/communities")
@CrossOrigin(origins = "http://localhost:5173")
public class CommunityController {

    private final CommunityService communityService;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CommunityController(
            CommunityService communityService,
            UserService userService,
            FileStorageService fileStorageService,
            ObjectMapper objectMapper
    ) {
        this.communityService = communityService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCommunity(@RequestAttribute("userId") Long ownerId, @RequestBody CreateCommunityRequest request) {
        try {
            Community community = communityService.createCommunity(
                    request.getName(),
                    request.getDescription(),
                    ownerId,
                    request.getTags(),
                    null
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(community);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCommunityWithImage(
            @RequestAttribute("userId") Long ownerId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "tags", required = false) String tagsRaw,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                if (imageFile.getContentType() == null || !imageFile.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed for community cover."));
                }
                long maxRecommendedSize = 10L * 1024L * 1024L;
                if (imageFile.getSize() > maxRecommendedSize) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Image should be 10MB or smaller."));
                }
                imageUrl = fileStorageService.storeFile(imageFile);
            }

            Community community = communityService.createCommunity(
                    name,
                    description,
                    ownerId,
                    parseTags(tagsRaw),
                    imageUrl
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(community);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping(value = "/{communityId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCommunity(
            @PathVariable Long communityId,
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateCommunityRequest request
    ) {
        try {
            Community community = communityService.updateCommunity(
                    communityId,
                    userId,
                    request.getName(),
                    request.getDescription(),
                    request.getTags(),
                    null,
                    Boolean.TRUE.equals(request.getRemoveImage())
            );
            return ResponseEntity.ok(community);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping(value = "/{communityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCommunityWithImage(
            @PathVariable Long communityId,
            @RequestAttribute("userId") Long userId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "tags", required = false) String tagsRaw,
            @RequestParam(value = "removeImage", required = false, defaultValue = "false") Boolean removeImage,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                if (imageFile.getContentType() == null || !imageFile.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed for community cover."));
                }
                long maxRecommendedSize = 10L * 1024L * 1024L;
                if (imageFile.getSize() > maxRecommendedSize) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Image should be 10MB or smaller."));
                }
                imageUrl = fileStorageService.storeFile(imageFile);
            }

            Community community = communityService.updateCommunity(
                    communityId,
                    userId,
                    name,
                    description,
                    parseTags(tagsRaw),
                    imageUrl,
                    Boolean.TRUE.equals(removeImage)
            );
            return ResponseEntity.ok(community);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{communityId}")
    public ResponseEntity<?> deleteCommunity(
            @PathVariable Long communityId,
            @RequestAttribute("userId") Long userId
    ) {
        try {
            communityService.deleteCommunity(communityId, userId);
            return ResponseEntity.ok(Map.of("message", "Community deleted successfully."));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Community>> getAllCommunities() {
        List<Community> communities = communityService.getAllCommunities();
        return ResponseEntity.ok(communities);
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<Community> getCommunityById(@PathVariable Long communityId) {
        return communityService.getCommunityById(communityId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{communityId}/join")
    public ResponseEntity<?> joinCommunity(@PathVariable Long communityId, @RequestAttribute("userId") Long userId) {
        try {
            CommunityMember communityMember = communityService.joinCommunity(communityId, userId);
            return ResponseEntity.ok(communityMember);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{communityId}/leave")
    public ResponseEntity<?> leaveCommunity(@PathVariable Long communityId, @RequestAttribute("userId") Long userId) {
        try {
            communityService.leaveCommunity(communityId, userId);
            return ResponseEntity.ok(Map.of("message", "Successfully left community."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-communities")
    public ResponseEntity<List<Community>> getMyCommunities(@RequestAttribute("userId") Long userId) {
        List<Community> communities = communityService.getCommunitiesByUserId(userId);
        return ResponseEntity.ok(communities);
    }

    @GetMapping("/{communityId}/members/count")
    public ResponseEntity<Long> getCommunityMemberCount(@PathVariable Long communityId) {
        long count = communityService.getCommunityMemberCount(communityId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{communityId}/is-member")
    public ResponseEntity<Boolean> isUserMemberOfCommunity(@PathVariable Long communityId, @RequestAttribute("userId") Long userId) {
        boolean isMember = communityService.isUserMemberOfCommunity(userId, communityId);
        return ResponseEntity.ok(isMember);
    }

    private List<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return List.of();
        }

        String normalized = rawTags.trim();
        if (normalized.startsWith("[")) {
            try {
                String[] parsed = objectMapper.readValue(normalized, String[].class);
                return Arrays.stream(parsed)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
            } catch (JsonProcessingException ignored) {
                // fallback to CSV parsing below
            }
        }

        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

}
