package com.example.biblov1.controller;

import com.example.biblov1.model.Like;
import com.example.biblov1.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@CrossOrigin(origins = "http://localhost:5173")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> toggleLikeForPost(@PathVariable Long postId, @RequestAttribute("userId") Long userId) {
        try {
            boolean isLiked = likeService.toggleLike(postId, userId);
            if (isLiked) {
                return ResponseEntity.ok(Map.of("message", "Post liked successfully.", "isLiked", true));
            } else {
                return ResponseEntity.ok(Map.of("message", "Post unliked successfully.", "isLiked", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getLikesCountForPost(@PathVariable Long postId) {
        long count = likeService.getLikesCount(postId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/post/{postId}/is-liked")
    public ResponseEntity<Boolean> isPostLikedByUser(@PathVariable Long postId, @RequestAttribute("userId") Long userId) {
        boolean isLiked = likeService.isPostLikedByUser(postId, userId);
        return ResponseEntity.ok(isLiked);
    }
} 