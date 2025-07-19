package com.example.biblov1.controller;

import com.example.biblov1.model.Community;
import com.example.biblov1.model.CommunityMember;
import com.example.biblov1.model.User;
import com.example.biblov1.service.CommunityService;
import com.example.biblov1.service.UserService;
import com.example.biblov1.payload.request.CreateCommunityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/communities")
@CrossOrigin(origins = "http://localhost:5173")
public class CommunityController {

    private final CommunityService communityService;
    private final UserService userService;

    @Autowired
    public CommunityController(CommunityService communityService, UserService userService) {
        this.communityService = communityService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createCommunity(@RequestAttribute("userId") Long ownerId, @RequestBody CreateCommunityRequest request) {
        try {
            Community community = communityService.createCommunity(request.getName(), request.getDescription(), ownerId, request.getTags());
            return ResponseEntity.status(HttpStatus.CREATED).body(community);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

} 